package com.mongodb.starter.services;

import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.repositories.MbotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MbotServiceImpl implements MbotService {

    private final MbotRepository mbotRepository;

    public MbotServiceImpl(MbotRepository mbotRepository) {
        this.mbotRepository = mbotRepository;
    }

    @Override
    public MbotDTO save(MbotDTO carDTO) {
        return new MbotDTO(mbotRepository.save(carDTO.toMbotEntity()));
    }

    @Override
    public List<MbotDTO> saveAll(List<MbotDTO> personEntities) {
        return personEntities.stream()
                .map(MbotDTO::toMbotEntity)
                .peek(mbotRepository::save)
                .map(MbotDTO::new)
                .toList();
    }

    @Override
    public List<MbotDTO> findAll() {
        return mbotRepository.findAll().stream().map(MbotDTO::new).toList();
    }

    @Override
    public List<MbotDTO> findAll(List<String> ids) {
        return mbotRepository.findAll(ids).stream().map(MbotDTO::new).toList();
    }

    @Override
    public MbotDTO findOne(String id) {
        return new MbotDTO(mbotRepository.findOne(id));
    }

    @Override
    public long count() {
        return mbotRepository.count();
    }

    @Override
    public long delete(String id) {
        return mbotRepository.delete(id);
    }

    @Override
    public long delete(List<String> ids) {
        return mbotRepository.delete(ids);
    }

    @Override
    public long deleteAll() {
        return mbotRepository.deleteAll();
    }

    @Override
    public MbotDTO update(MbotDTO MbotDTO) {
        return new MbotDTO(mbotRepository.update(MbotDTO.toMbotEntity()));
    }

    @Override
    public long update(List<MbotDTO> personEntities) {
        return mbotRepository.update(personEntities.stream().map(MbotDTO::toMbotEntity).toList());
    }

    @Override
    public double getAverageAge() {
        return mbotRepository.getAverageAge();
    }
}