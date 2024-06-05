package com.mongodb.starter.dtos;

import com.mongodb.starter.ConnectionType;
import com.mongodb.starter.models.MbotEntity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;


@Getter
@Setter
public class MbotDTO {
    private float ultrasonic;
    private ArrayList<Integer> angles;
    private int sound;
    private ArrayList<Integer> front_light_sensors;
    private int shake;
    private int light;
    private ConnectionType type;
    private String ip;

    public MbotDTO(){}

    // Constructor with parameters
    public MbotDTO(float ultrasonic, ArrayList<Integer> angles, int sound, 
        ArrayList<Integer> front_light_sensors, int shake, int light, 
        ConnectionType type, String ip) {
        this.ultrasonic = ultrasonic;
        this.angles = angles;
        this.sound = sound;
        this.front_light_sensors = front_light_sensors;
        this.shake = shake;
        this.light = light;
        this.type = type;
        this.ip = ip;
    }

    public MbotDTO(MbotEntity c) {
        this.ultrasonic = c.getUltrasonic();
        this.angles = c.getAngles();
        this.sound = c.getSound();
        this.front_light_sensors = c.getFront_light_sensors();
        this.shake = c.getShake();
        this.light = c.getLight();
        this.type = c.getType();
        this.ip = c.getIP();
    }

    public MbotEntity toMbotEntity() {
        return new MbotEntity(ultrasonic, angles, sound, front_light_sensors, shake, light, type, ip);
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
                ", type=" + type +
                '}';
    }
}
