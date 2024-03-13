//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mongodb.starter.services;

import com.mongodb.starter.dtos.MbotDTO;
import java.util.List;

public interface MbotService {
    MbotDTO save(MbotDTO carDTO);

    List<MbotDTO> saveAll(List<MbotDTO> carEntities);

    List<MbotDTO> findAll();

    List<MbotDTO> findAll(List<String> ids);

    MbotDTO findOne(String id);

    long count();

    long delete(String id);

    long delete(List<String> ids);

    long deleteAll();

    MbotDTO update(MbotDTO carDTO);

    long update(List<MbotDTO> CarEntities);

    double getAverageAge();
}
