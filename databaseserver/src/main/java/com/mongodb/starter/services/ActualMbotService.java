package com.mongodb.starter.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.repositories.ActualMbotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActualMbotService {
    private final ActualMbotRepository repository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ActualMbotService.class);

    private ObjectMapper objectMapper;

    private MongoTemplate mongoTemplate;

    @Autowired
    public ActualMbotService(ActualMbotRepository repo, MongoTemplate template){
        this.repository = repo;
        this.mongoTemplate = template;
    }

    public List<MbotDTO> findAll(){
        return repository.findAll();
    }

    public MbotDTO save(MbotDTO m){
        return repository.save(m);
    }

    public void deleteAll(){
        repository.deleteAll();
    }

}
