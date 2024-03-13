package com.mongodb.starter.dtos;

import com.mongodb.starter.models.MbotEntity;
import org.bson.types.ObjectId;

public record MbotDTO(float ultrasonic, int[] angles, int sound, int[] front_light_sensors,
                      int shake, int light, ObjectId id) {

    public MbotDTO(MbotEntity c) {
        this(c.getUltrasonic(), c.getAngles(), c.getSound(), c.getFront_light_sensors(), c.getShake(), c.getLight(), c.getId());
    }

    public MbotEntity toMbotEntity() {
        return new MbotEntity(ultrasonic, angles, sound, front_light_sensors, shake, light, id);
    }
}
