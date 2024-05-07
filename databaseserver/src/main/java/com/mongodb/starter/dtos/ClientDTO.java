package com.mongodb.starter.dtos;

import com.mongodb.starter.ConnectionType;
import com.mongodb.starter.models.MbotEntity;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public record ClientDTO(float ultrasonic, ArrayList<Integer> angles, int sound, ArrayList<Integer> front_light_sensors,
                        int shake, int light, ConnectionType type, String IP) {


    public ClientDTO(MbotDTO c) {
        this(c.ultrasonic(), c.angles(), c.sound(), c.front_light_sensors(), c.shake(), c.light(), c.toMbotEntity().getIP());
    }

    public MbotEntity toMbotEntity() {
        ObjectId _id = new ObjectId();
        return new MbotEntity(ultrasonic, angles, sound, front_light_sensors, shake, light, IP, _id);
    }

}
