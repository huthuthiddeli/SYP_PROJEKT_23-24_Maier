package com.mongodb.starter.models;

import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.Objects;

public class MbotEntity {
    private float ultrasonic;
    private int[] angles;
    private int sound;
    private int[] front_light_sensors;
    private int shake;
    private int light;
    private ObjectId id;

    public MbotEntity(){}

    public MbotEntity(float ultrasonic, int[] angles, int sound, int[] front_light_sensors, int shake,
                      int light){
        this.ultrasonic = ultrasonic;
        this.angles = angles;
        this.sound = sound;
        this.front_light_sensors = front_light_sensors;
        this.shake = shake;
        this.light = light;
    }

    public MbotEntity(float ultrasonic, int[] angles, int sound, int[] front_light_sensors, int shake,
                      int light, ObjectId id) {
        this.ultrasonic = ultrasonic;
        this.angles = angles;
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

    public int[] getAngles() {
        return angles;
    }

    public MbotEntity setAngles(int[] angles) {
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

    public int[] getFront_light_sensors() {
        return front_light_sensors;
    }

    public MbotEntity setFront_light_sensors(int[] front_light_sensors) {
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
                Arrays.equals(front_light_sensors, MbotEntity.front_light_sensors) && sound == MbotEntity.sound &&
                Arrays.equals(angles, MbotEntity.angles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ultrasonic, angles, sound, front_light_sensors, shake, light);
    }

    @Override
    public String toString() {
        return "MbotDataEntity{" +
                "ultrasonic=" + ultrasonic +
                ", angles=" + Arrays.toString(angles) +
                ", sound=" + sound +
                ", front_light_sensors=" + Arrays.toString(front_light_sensors) +
                ", shake=" + shake +
                ", light=" + light +
                '}';
    }
}
