package com.mongodb.starter.dtos;

import com.mongodb.starter.ConnectionTypes;
import com.mongodb.starter.models.MbotEntity;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public record ClientDTO(float ultrasonic, ArrayList<Integer> angles, int sound, ArrayList<Integer> front_light_sensors,
                        int shake, int light, ConnectionTypes type,String IP) {


    public ClientDTO(MbotDTO c) {
        this(c.ultrasonic(), c.angles(), c.sound(), c.front_light_sensors(), c.shake(), c.light(), c.type(),c.toMbotEntity().getIP());
    }

    public MbotEntity toMbotEntity() {
        ObjectId _id = new ObjectId();
        return new MbotEntity(ultrasonic, angles, sound, front_light_sensors, shake, light, IP, type, _id);
    }

}
