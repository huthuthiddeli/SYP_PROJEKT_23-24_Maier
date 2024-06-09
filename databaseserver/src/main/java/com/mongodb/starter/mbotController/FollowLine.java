package com.mongodb.starter.mbotController;

import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.Command;

import java.net.InetAddress;
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

    public Command LINEFOLLOW(MbotDTO dto, InetAddress address){
        Command command = new Command();



        if(frontLightSensors.get(0) > 50 && frontLightSensors.get(2) > 50){
            command.setName("20;-20!");
            command.setSocket("");
        }else if(frontLightSensors.get(2) > 50){
            command.setSocket("20;-5!");
        }

        command.setSocket(address.toString());

        return command;
    }




}
