package com.mongodb.starter.repositories;

import com.mongodb.starter.models.MbotEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MbotRepository {

    MbotEntity save(MbotEntity MbotEntity);

    List<MbotEntity> saveAll(List<MbotEntity> personEntities);

    List<MbotEntity> findAll();

    List<MbotEntity> findAll(List<String> ids);

    MbotEntity findOne(String id);

    long count();

    long delete(String id);

    long delete(List<String> ids);

    long deleteAll();

    MbotEntity update(MbotEntity MbotEntity);

    long update(List<MbotEntity> personEntities);
}
