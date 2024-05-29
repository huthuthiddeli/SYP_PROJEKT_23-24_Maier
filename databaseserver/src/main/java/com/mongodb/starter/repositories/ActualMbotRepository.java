package com.mongodb.starter.repositories;


import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.MbotEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActualMbotRepository extends MongoRepository<MbotDTO, String> {


    List<MbotDTO> findAll();
    MbotDTO save(MbotDTO m);

    void deleteAll();
}
