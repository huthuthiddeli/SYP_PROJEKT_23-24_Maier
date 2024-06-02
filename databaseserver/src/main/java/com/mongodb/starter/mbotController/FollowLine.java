package com.mongodb.starter.mbotController;

import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.Command;

import java.net.Socket;
import java.util.ArrayList;

public class FollowLine {
    private static FollowLine INSTANCE;

    private FollowLine(){}


    public static FollowLine getINSTANCE() {
        if(INSTANCE == null){
            INSTANCE = new FollowLine();
        }

        return INSTANCE;
    }

    public Command LINEFOLLOW(MbotDTO dto){
        Command command = new Command();


        ArrayList<Integer> frontLightSensors = dto.getFront_light_sensors();

        if(frontLightSensors.get(0) > 50 && frontLightSensors.get(2) > 50){
            command.setName("20;-20!");
            command.setSocket("");
        }else if(frontLightSensors.get(0) > 50){
            command.setSocket("5;-20!");
        }else if(frontLightSensors.get(2) > 50){
            command.setSocket("20;-5!");
        }
        return command;
    }





}
