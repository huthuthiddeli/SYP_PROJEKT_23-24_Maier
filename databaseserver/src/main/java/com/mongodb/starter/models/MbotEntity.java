package com.mongodb.starter.models;

import com.mongodb.starter.ConnectionType;
import com.mongodb.starter.dtos.MbotDTO;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;



@Document(collation = "/mbots")
@Getter
@Setter
public class MbotEntity {
    @Id
    private String id;
    @Field("ultrasonic")
    private float ultrasonic;
    @Field("angles")
    private ArrayList<Integer> angles;
    @Field("sound")
    private int sound;
    @Field("font_light_sensors")
    private ArrayList<Integer> front_light_sensors;
    @Field("shake")
    private int shake;
    @Field("light")
    private int light;
    @Field("IP")
    private String IP;
    @Field("type")
    private ConnectionType type;

    public MbotEntity(){}

    public MbotEntity(float ultrasonic, ArrayList<Integer> angle, int sound, ArrayList<Integer> front_light_sensors, int shake,
                      int light, String IP, ConnectionType type , String id){
        this.ultrasonic = ultrasonic;
        this.angles = angle;
        this.sound = sound;
        this.front_light_sensors = front_light_sensors;
        this.shake = shake;
        this.light = light;
        this.IP = IP;
        this.id = id;
    }

    public MbotEntity(float ultrasonic, ArrayList<Integer> angles, int sound, ArrayList<Integer> frontLightSensors, int shake, int light, ConnectionType type, String ip) {
        this.ultrasonic = ultrasonic;
        this.angles = angles;
        this.sound = sound;
        this.front_light_sensors = frontLightSensors;
        this.shake = shake;
        this.light = light;
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

    public ConnectionType getType(){
        return this.type;
    }

    public MbotEntity setType(ConnectionType newType){
        this.type = newType;
        return this;
    }

    public MbotEntity setId(String id){
        this.id = id;
        return this;
    }

    public String getId(){
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
