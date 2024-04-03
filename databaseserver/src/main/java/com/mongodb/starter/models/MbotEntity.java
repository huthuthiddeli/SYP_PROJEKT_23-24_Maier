package com.mongodb.starter.models;

import org.bson.types.ObjectId;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MbotEntity {
    private float ultrasonic;
    private ArrayList<Integer> angles;
    private int sound;
    private ArrayList<Integer> front_light_sensors;
    private int shake;
    private int light;
    private ObjectId id;

    public MbotEntity(){}

    public MbotEntity(float ultrasonic, ArrayList<Integer> angle, int sound, ArrayList<Integer> front_light_sensors, int shake,
                      int light){
        this.ultrasonic = ultrasonic;
        this.angles = angle;
        this.sound = sound;
        this.front_light_sensors = front_light_sensors;
        this.shake = shake;
        this.light = light;
    }

    public MbotEntity(float ultrasonic, ArrayList<Integer> angles, int sound, ArrayList<Integer> front_light_sensors, int shake,
                      int light, ObjectId id) {
        this.ultrasonic = ultrasonic;
        this.angles = front_light_sensors;
        this.sound = sound;
        this.front_light_sensors = front_light_sensors;
        this.shake = shake;
        this.light = light;
        this.id = id;
    }

    public float getUltrasonic() {
        return ultrasonic;
    }

    public MbotEntity setUltrasonic(float ultrasonic) {
        this.ultrasonic = ultrasonic;
        return this;
    }

    public ArrayList<Integer> getAngles() {
        return angles;
    }

    public MbotEntity setAngles(ArrayList<Integer> angles) {
        this.angles = angles;
        return this;
    }

    public int getSound() {
        return sound;
    }

    public MbotEntity setSound(int sound) {
        this.sound = sound;
        return this;
    }

    public ObjectId getId(){
        return this.id;
    }

    public MbotEntity setId(ObjectId id){
        this.id = id;
        return this;
    }

    public ArrayList<Integer> getFront_light_sensors() {
        return front_light_sensors;
    }

    public MbotEntity setFront_light_sensors(ArrayList<Integer> front_light_sensors) {
        this.front_light_sensors = front_light_sensors;
        return this;
    }

    public int getShake() {
        return shake;
    }

    public MbotEntity setShake(int shake) {
        this.shake = shake;
        return this;
    }

    public int getLight() {
        return light;
    }

    public MbotEntity setLight(int light) {
        this.light = light;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MbotEntity MbotEntity = (MbotEntity) o;
        //return Objects.equals(ultrasonic, MbotEntity.ip) && Objects.equals(model, MbotEntity.model) && Objects.equals(
                //maxSpeedKmH, MbotEntity.maxSpeedKmH);

        return ultrasonic == MbotEntity.ultrasonic && shake == MbotEntity.shake && light == MbotEntity.light &&
                front_light_sensors.equals(MbotEntity.front_light_sensors) && sound == MbotEntity.sound &&
                angles.equals(MbotEntity.angles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ultrasonic, angles, sound, front_light_sensors, shake, light);
    }

    @Override
    public String toString() {
        return "MbotDataEntity{" +
                "ultrasonic=" + ultrasonic +
                ", angles=" + angles.toString() +
                ", sound=" + sound +
                ", front_light_sensors=" + front_light_sensors.toString() +
                ", shake=" + shake +
                ", light=" + light +
                '}';
    }
}
