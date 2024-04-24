package com.mongodb.starter.repositories;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.starter.models.MbotEntity;
import com.mongodb.starter.repositories.MbotRepository;
import jakarta.annotation.PostConstruct;
import org.bson.BsonDocument;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.ReturnDocument.AFTER;

@Repository
public class MongoDBCarRepository implements MbotRepository {

    private static final TransactionOptions txnOptions = TransactionOptions.builder()
            .readPreference(ReadPreference.primary())
            .readConcern(ReadConcern.MAJORITY)
            .writeConcern(WriteConcern.MAJORITY)
            .build();
    private final MongoClient client;
    private MongoCollection<MbotEntity> personCollection;

    public MongoDBCarRepository(MongoClient mongoClient) {
        this.client = mongoClient;
    }

    @PostConstruct
    void init() {
        personCollection = client.getDatabase("SYP_2024_FAL_EIG").getCollection("MBotEntry", MbotEntity.class);
    }

    @Override
    public MbotEntity save(MbotEntity MbotEntity) {
        MbotEntity.setId(new ObjectId());
        personCollection.insertOne(MbotEntity);
        return MbotEntity;
    }

    @Override
    public List<MbotEntity> saveAll(List<MbotEntity> personEntities) {
        try (ClientSession clientSession = client.startSession()) {
            return clientSession.withTransaction(() -> {
                personEntities.forEach(c -> c.setId(new ObjectId()));
                personCollection.insertMany(clientSession, personEntities);
                return personEntities;
            }, txnOptions);
        }
    }

    @Override
    public List<MbotEntity> findAll() {
        return personCollection.find().into(new ArrayList<>());
    }

    @Override
    public List<MbotEntity> findAll(List<String> ids) {
        return personCollection.find(in("_id", mapToObjectIds(ids))).into(new ArrayList<>());
    }

    @Override
    public MbotEntity findOne(String id) {
        return personCollection.find(eq("_id", new ObjectId(id))).first();
    }

    @Override
    public long count() {
        return personCollection.countDocuments();
    }

    @Override
    public long delete(String id) {
        return personCollection.deleteOne(eq("_id", new ObjectId(id))).getDeletedCount();
    }

    @Override
    public long delete(List<String> ids) {
        try (ClientSession clientSession = client.startSession()) {
            return clientSession.withTransaction(
                    () -> personCollection.deleteMany(clientSession, in("_id", mapToObjectIds(ids))).getDeletedCount(),
                    txnOptions);
        }
    }

    @Override
    public long deleteAll() {
        try (ClientSession clientSession = client.startSession()) {
            return clientSession.withTransaction(
                    () -> personCollection.deleteMany(clientSession, new BsonDocument()).getDeletedCount(), txnOptions);
        }
    }

    @Override
    public MbotEntity update(MbotEntity mbotEntity) {
        FindOneAndReplaceOptions options = new FindOneAndReplaceOptions().returnDocument(AFTER);
        return personCollection.findOneAndReplace(eq("_id", mbotEntity.getId()), mbotEntity, options);
    }

    @Override
    public long update(List<MbotEntity> personEntities) {
        List<ReplaceOneModel<MbotEntity>> writes = personEntities.stream()
                .map(p -> new ReplaceOneModel<>(eq("_id", p.getId()), p))
                .toList();
        try (ClientSession clientSession = client.startSession()) {
            return clientSession.withTransaction(
                    () -> personCollection.bulkWrite(clientSession, writes).getModifiedCount(), txnOptions);
        }
    }

    @Override
    public double getAverageAge() {
        return 0;
    }

    private List<ObjectId> mapToObjectIds(List<String> ids) {
        return ids.stream().map(ObjectId::new).toList();
    }
}