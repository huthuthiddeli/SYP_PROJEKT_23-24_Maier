package com.mongodb.starter.Networking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.starter.ConnectionType;
import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.Command;
import com.mongodb.starter.models.MbotEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server {

    private static Server INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private ArrayList<Socket> connectedSockets = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();




    private boolean LINEFOLLOWER = false;
    private OutputStream stream;
    private int prevSize = 0;
    private int counter = 0;

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
                    LOGGER.info("[SERVER]\t\t\tSocket: " + clientSocket.getInetAddress().toString() + " is already registered!");
                    continue;
                }
                
                IsStillConnected();
            }
        } catch (IOException e) {
           LOGGER.error("[SERVER]\t\t\tError occurred while running the server: " + e.getMessage());
        } catch (ConcurrentModificationException e){
            LOGGER.error("[SERVER]\t\t\t Error: " + e.getMessage());
        }
    }

    public boolean SendCommandToClient(Command command) throws IOException, InterruptedException {
        Socket s = DetermineRightSocketMbot(command);

        //WHEN SOCKET NULL RETURN
        if(s == null){
            LOGGER.info("[SERVER]\t\t\tSockets: " +  s.toString());
            return false;
        }


        try {
            //INFO WHEN CONNECTED SOCKETS CHANGES
            if(prevSize == 0 || prevSize != connectedSockets.size()){
                LOGGER.info("[SERVER]\t\t\t" + String.valueOf("Connected Devices: " + connectedSockets.size()));
                prevSize = connectedSockets.size();
            }

            stream = s.getOutputStream();



            if(LINEFOLLOWER){

            }else{
                stream.write(command.getName().getBytes(StandardCharsets.UTF_8));
            }

            stream.flush();

            if(counter >= 25){
                LOGGER.info("[SERVER]\t\t\tCommand: " + command.toString() + " sent to: " + s.toString());
                counter = 0;
            }else{
                counter++;
            }


        }catch (Exception ex){
            LOGGER.error("[SERVER]\t\t\tERROR:" + ex.getMessage());
            LOGGER.error("[SERVER]\t\t\tRestart Mbot with ip: " + command.getSocket());
            return false;
        }

        return true;
    }

    @Scheduled(fixedDelay = 1000)
    public boolean SendSensorDataToClient(MbotEntity m) throws NullPointerException, InterruptedException, IOException {
        Thread.sleep(1000);

        String clientSocketString = BroadcastServer.getClientSocket().split(":")[0];
        Socket s = new Socket();

        try{

            s = DetermineRightSocketClient(clientSocketString);

            if(s == null){
                if(counter > 25){
                    LOGGER.info("[SERVER]\t\t\tThere was no client registered!");
                    counter = 0;
                }else if(counter > 12){
                    LOGGER.info("[SERVER]\t\t\tData sent to not existing client!");
                }else{
                    counter++;
                }

                return false;
            }

            stream = s.getOutputStream();

            m = new MbotDTO(
                    m.getUltrasonic(),
                    m.getAngles(),
                    m.getSound(),
                    m.getFront_light_sensors(),
                    m.getShake(),
                    m.getLight(),
                    ConnectionType.CONNECTION_ALIVE,
                    m.getIP()
            )
                    .toMbotEntity();

            stream.write(mapper.writeValueAsBytes(m));
            stream.flush();

        } catch (IOException e) {
            System.out.println("Client is not alive: " + e.getMessage());
            LOGGER.error("[SERVER]\t\t\tError sending data to client (SensordatatTOClient): " + e.getMessage());
            return false;
        } catch(Exception ex){
            LOGGER.error("[SERVER]\t\t\tCritical error occured!");

            return false;
        }


        return true;
    }


    private void IsStillConnected() throws IOException {
        for(Socket s : connectedSockets){
            try {
                s.getOutputStream().write(1);
                s.getOutputStream().flush();
            } catch (IOException e) {
                System.out.println("Socket is not alive: " + e.getMessage());
                connectedSockets.remove(s);
                for(InetAddress broadcastSocket : BroadcastServer.getMbotSockets()){
                    if(Objects.equals(broadcastSocket.toString(), s.getInetAddress().toString())){
                        LOGGER.info("[SERVER]\t\tSocket has been disconnected: " + broadcastSocket.toString());
                        BroadcastServer.getMbotSockets().remove(broadcastSocket);
                    }
                }
            }
        }
    }

    //IF IP IS ALREADY IN LIST SKIP ACTIONS
    private boolean IsRegisteredSocket(Socket address) throws IOException{
        if(address == null){
            return false;
        }

        for(Socket s : connectedSockets){
            IsStillConnected();

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

    private Socket DetermineRightSocketMbot(Command command){
        if(connectedSockets.isEmpty()){
            LOGGER.error("[SERVER]\t\t\tNO SOCKETS!");
        }

        for (Socket connectedSocket : connectedSockets) {
            if (IsRegisteredString(command.getSocket())) {
                return connectedSocket;
            }
        }

        if(counter >= 25){
            LOGGER.error("[SERVER]\t\t\tNo Mbot-Sockets matching!");
            counter = 0;
        }else{
            counter++;
        }

        return null;
    }

    private Socket DetermineRightSocketClient(String address){
        for(Socket s : connectedSockets){
            if(Objects.equals(s.getInetAddress().toString(), address)){
                return s;
            }
        }
        if(counter > 25){
            LOGGER.info("[SERVER]\t\t\tNo Client-Sockets matching!");
            counter = 0;
        }else{
            counter++;
        }


        return null;
    }

    public boolean SendSuicideToggle(Command command, boolean status){
        Socket s = DetermineRightSocketClient(command.getSocket());

        if(s == null){
            LOGGER.error("[SERVER]\t\t\tThere was no MBOT to toggle suicide prevention");
            return false;
        }

        try{
            stream = s.getOutputStream();

            if(!status){
                stream.write("ANTISUICIDE_ON".getBytes(StandardCharsets.UTF_8));
            }else{
                stream.write("ANTISUICIDE_OFF".getBytes(StandardCharsets.UTF_8));
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return true;
    }

    public static Server getServer(){
        if(INSTANCE == null){
            INSTANCE = new Server();
        }

        return INSTANCE;
    }
}