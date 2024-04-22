package com.mongodb.starter.Networking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BroadcastServer {
    private static String clientSocket;
    private static ArrayList<InetAddress> mbotSockets = new ArrayList<>();


    private static Logger LOGGER = LoggerFactory.getLogger(BroadcastServer.class);


    public static void RUN(){
        // Define the port number to listen on
        int port = 5595;

        try {
            // Create a socket to listen for broadcasts UDP
            DatagramSocket socket = new DatagramSocket(port);
            LOGGER.info("[BROADCAST]\t\tListening for broadcast messages on port " + port);

            while (true) {
                // Create a buffer to store incoming data
                byte[] buffer = new byte[1024];

                // Create a DatagramPacket to receive data
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // Receive the data
                socket.receive(packet);

                // Convert the data to a string
                String receivedMessage = new String(packet.getData(), 0, packet.getLength());

                // Get the sender's address and port
                InetAddress senderAddress = packet.getAddress();
                int senderPort = packet.getPort();

                // Print the received message and sender's information
                LOGGER.info("[BROADCAST]\t\tReceived message from " + senderAddress + ":" + senderPort + " - " + receivedMessage);


                if (receivedMessage.trim().equals("ACM 6000")){
                    String responseMessage = "ACM";
                    byte[] responseData = responseMessage.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, senderAddress, 6000);
                    socket.send(responsePacket);
                    LOGGER.info("[BROADCAST]\t\tResponse: " + senderAddress + ":" + senderPort + " - " + responseMessage);
                    mbotSockets.add(senderAddress);
                }

                if(receivedMessage.trim().equals("ACC")){
                    String responseMessage = "ACCACK";
                    byte[] responseData = responseMessage.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, senderAddress, senderPort);
                    socket.send(responsePacket);
                    LOGGER.info("[BROADCAST]\t\tResponse: " + senderAddress + ":" + senderPort + " - " + responseMessage);
                    clientSocket = senderAddress + ":" + senderPort;
                }
            }
        } catch (IOException e) {
           LOGGER.error("[BROADCAST]\t\tError: " + e.getMessage());
        }catch(IllegalArgumentException e){
           LOGGER.error("[BROADCAST]\t\t" + e.getMessage());
        }
    }

    public static String getClientSocket(){
        return clientSocket;
    }

    public static ArrayList<InetAddress> getMbotSockets(){
        return mbotSockets;
    }
}