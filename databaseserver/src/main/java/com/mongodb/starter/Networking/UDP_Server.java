package com.mongodb.starter.Networking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UDP_Server {

    private static UDP_Server INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private DatagramSocket serverSocket;

    private UDP_Server(){}

    public void RUN(){
        int PORT = 5000;

        try{
            serverSocket = new DatagramSocket(PORT);
            LOGGER.info("[UDP_Server]\t\tListening for UDP-Connection on port: " + PORT);

            while(true){
                byte[] buffer = new byte[1024];

                DatagramPacket packet =  new DatagramPacket(buffer, buffer.length);

                serverSocket.receive(packet);

                InetAddress senderAddress = packet.getAddress();
                int senderPort = packet.getPort();


                LOGGER.info("[UDP-SERVER]\t\tMessage received from: " + senderAddress + ":" + senderPort);

                String awnser = "Alive";
                byte[] responseData = awnser.getBytes(StandardCharsets.UTF_8);
                DatagramPacket responsePackage = new DatagramPacket(responseData, responseData.length);

                serverSocket.send(responsePackage);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public UDP_Server GetInstance(){
        if(INSTANCE == null){
            INSTANCE = new UDP_Server();
        }

        return INSTANCE;
    }

}