package com.mongodb.starter.Networking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.Command;
import com.mongodb.starter.models.MbotEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server {

    private static Server INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private ArrayList<Command> commandList = new ArrayList<>();

    private ArrayList<Socket> connectedSockets = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private OutputStream stream;

    private int prevSize = 0;


    private Server(){}

    public void RUN() {
        int portNumber = 5000;

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            LOGGER.info("[SERVER]\t\t\tTCP-Server started on port " + portNumber);

            while(true) {
                Socket clientSocket = serverSocket.accept(); // Accept incoming client connections

                LOGGER.info("[SERVER]\t\t\tConnection from: " + clientSocket.getInetAddress().getHostAddress());

                //check if ip address is already registered
                if(IsRegisteredSocket(clientSocket)){
                    continue;
                }
                
                IsStillConnected();
            }
        } catch (IOException e) {
           LOGGER.error("[SERVER]\t\t\tError occurred while running the server: " + e.getMessage());
        } catch (ConcurrentModificationException e){
            LOGGER.error("[SERVER]\t\t\t" + e.getMessage());
        }
    }

    public boolean SendCommandToClient(Command command) throws IOException, InterruptedException {
        Socket s = TetermineRightSocketMbot(command);

        if(s == null){
            LOGGER.info("[SERVER]\t\t\tSockets: " +  s.toString());
            return false;
        }


        try {
            //INFO WHEN CONNECTED SOCKETS INCREASE
            if(prevSize == 0 || prevSize != connectedSockets.size()){
                LOGGER.info("[SERVER]\t\t\t" + String.valueOf("Size of List: " + connectedSockets.size()));
                prevSize = connectedSockets.size();
            }

            //DELETE IF THE CONNECTION IS NOT UP ANYMORE
            if(!s.isConnected()){
                LOGGER.info("[SERVER]\t\t\tIs not connected anymore!");
                connectedSockets.remove(s);
            }
            stream = s.getOutputStream();

            stream.write(command.getName().getBytes(StandardCharsets.UTF_8));
            stream.flush();

            LOGGER.info("[SERVER]\t\t\tCommand: " + command.toString() + " sent to: " + s.toString());


        }catch (Exception ex){
            LOGGER.error("[SERVER]\t\t\tERROR:" + ex.getMessage());
            LOGGER.error("[SERVER]\t\t\tRestart Mbot with ip: " + command.getSocket());

            return false;
        }

        return true;
    }

    public boolean SendSensorDataToClient(MbotEntity m){
        String clientSocketString = BroadcastServer.getClientSocket().split(":")[0];

        try{

            InetAddress clientSocket = InetAddress.getByAddress(clientSocketString.getBytes(StandardCharsets.UTF_8));

            Socket s = TetermineRightSocketClient(clientSocket);

            if(s == null){
                LOGGER.info("[SERVER]\t\t\tThere was no such client registered: " + clientSocket.toString());
                return false;
            }

            stream = s.getOutputStream();
            stream.write(mapper.writeValueAsBytes(m));
            stream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return true;
    }



    private void IsStillConnected() throws IOException {
        for(Socket s : connectedSockets){
            if(!s.isConnected()){
                s.close();
            }
        }
    }

    //IF IP IS ALREADY IN LIST SKIP ACTIONS
    private boolean IsRegisteredSocket(Socket address){
        if(address == null){
            return false;
        }

        for(Socket s : connectedSockets){
            if(Objects.equals(s.getInetAddress(), address.getInetAddress())){
                return true;
            }
        }

        connectedSockets.add(address);

        return false;
    }

    private boolean IsRegisteredString(String socket){
        for(Socket s : connectedSockets) {
            if (Objects.equals(s.getInetAddress().toString(), socket)) {

                if(!s.isConnected()){
                    connectedSockets.remove(s);
                    return false;
                }

                return true;
            }
        }

        return false;
    }

    private Socket TetermineRightSocketMbot(Command command){
        if(connectedSockets.isEmpty()){
            LOGGER.error("[SERVER]\t\t\tNO SOCKETS!");
        }

        for (Socket connectedSocket : connectedSockets) {
            if (IsRegisteredString(command.getSocket())) {
                return connectedSocket;
            }
        }

        LOGGER.error("[SERVER]\t\t\tNo Mbot-Sockets matching!");

        return null;
    }

    private Socket TetermineRightSocketClient(InetAddress address){
        for(Socket s : connectedSockets){
            if(Objects.equals(s.getInetAddress().toString(), address.toString())){
                return s;
            }
        }

        LOGGER.info("[SERVER]\t\t\tNo Client-Sockets matching!");

        return null;
    }

    public static Server getServer(){
        if(INSTANCE == null){
            INSTANCE = new Server();
        }

        return INSTANCE;
    }

    public void addToCommandList(Command m){
        this.commandList.add(m);
    }
}