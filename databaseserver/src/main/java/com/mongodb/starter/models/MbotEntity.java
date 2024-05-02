package com.mongodb.starter.models;

import com.mongodb.starter.ConnectionTypes;
import org.bson.types.ObjectId;

import java.lang.reflect.Array;
import java.util.*;

public class MbotEntity {
    private ObjectId id;
    private float ultrasonic;
    private ArrayList<Integer> angles;
    private int sound;
    private ArrayList<Integer> front_light_sensors;
    private int shake;
    private int light;
    private String IP;
    private ConnectionTypes type;

    public MbotEntity(){}

    public MbotEntity(float ultrasonic, ArrayList<Integer> angle, int sound, ArrayList<Integer> front_light_sensors, int shake,
                      int light, String IP, ConnectionTypes type ,ObjectId id){
        this.ultrasonic = ultrasonic;
        this.angles = angle;
        this.sound = sound;
        this.front_light_sensors = front_light_sensors;
        this.shake = shake;
        this.light = light;
        this.IP = IP;
        this.type = type;
        this.id = id;
    }

    public MbotEntity(float ultrasonic, ArrayList<Integer> angles, int sound, ArrayList<Integer> frontLightSensors, int shake, int light, ConnectionTypes type, String ip) {
        this.ultrasonic = ultrasonic;
        this.angles = angles;
        this.sound = sound;
        this.front_light_sensors = frontLightSensors;
        this.shake = shake;
        this.light = light;
        this.type = type;
        this.IP = ip;
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

    public String getIP(){
        return this.IP;
    }

    public MbotEntity setIP(String IP){
        this.IP = IP;
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

    public ConnectionTypes getType(){
        return this.type;
    }

    public MbotEntity setType(ConnectionTypes newType){
        this.type = newType;
        return this;
    }

    public MbotEntity setId(ObjectId id){
        this.id = id;
        return this;
    }

    public ObjectId getId(){
        return this.id;
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
