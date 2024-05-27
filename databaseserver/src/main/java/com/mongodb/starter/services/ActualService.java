package com.mongodb.starter.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.MbotEntity;
import com.mongodb.starter.repositories.ActualMbotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActualService {

    private final ActualMbotRepository repository;

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MongoTemplate template;

    @Autowired
    public ActualService(ActualMbotRepository repo, MongoTemplate template){
        this.repository = repo;
        this.template = template;
    }

    public List<MbotEntity> findAll(){
        Query query = new Query();
        // Ensure no collation or locale is being set improperly
        return this.template.find(query, MbotEntity.class);
    }



    public MbotDTO save(MbotDTO s){
        return this.repository.save(s);
    }

    public void deleteAll(){
        this.repository.deleteAll();
    }
}
