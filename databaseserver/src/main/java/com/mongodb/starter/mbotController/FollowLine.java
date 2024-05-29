package com.mongodb.starter.mbotController;

import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.Command;

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
        Command command = null;



        return command;
    }





}
