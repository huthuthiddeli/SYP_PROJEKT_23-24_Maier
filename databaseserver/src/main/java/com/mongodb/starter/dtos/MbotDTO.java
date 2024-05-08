package com.mongodb.starter.dtos;

import com.mongodb.starter.ConnectionType;
import com.mongodb.starter.models.MbotEntity;

import java.util.ArrayList;

public record MbotDTO(float ultrasonic, ArrayList<Integer> angles, int sound, ArrayList<Integer> front_light_sensors,
                      int shake, int light, ConnectionType type, String IP) {

    public MbotDTO(MbotEntity c) {
        this(c.getUltrasonic(), c.getAngles(), c.getSound(), c.getFront_light_sensors(), c.getShake(), c.getLight(), c.getIP());
    }

    public MbotEntity toMbotEntity() {
        //ObjectId _id = objectID == null ? new ObjectId() : new ObjectId(objectID);
        return new MbotEntity(ultrasonic, angles, sound, front_light_sensors, shake, light, type, IP);
    }

    @Override
    public String toString() {
        return "MyRecord{" +
                "ultrasonic=" + ultrasonic +
                ", angles=" + angles +
                ", sound=" + sound +
                ", front_light_sensors=" + front_light_sensors +
                ", shake=" + shake +
                ", light=" + light +
                ", IP='" + IP + '\'' +
                '}';
    }
}
