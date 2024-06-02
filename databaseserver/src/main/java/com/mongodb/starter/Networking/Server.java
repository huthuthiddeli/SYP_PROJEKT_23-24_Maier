package com.mongodb.starter.Networking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.starter.ConnectionType;
import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.Command;
import com.mongodb.starter.models.MbotEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.ConsoleHandler;

public class Server {

    private static Server INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private ArrayList<Socket> connectedSockets = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private OutputStream stream;
    private static boolean preventCollision = false;
    private static boolean autoPilot = false;
    private static MbotDTO lastPackage;

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

                LOGGER.info("[SERVER]\t\t\tNew Connection on: " + clientSocket.getInetAddress().toString());

                //check if ip address is already registered
                if(IsRegisteredSocket(clientSocket)){
                    LOGGER.info("[SERVER]\t\t\tSocket: " + clientSocket.getInetAddress().toString() + " is already registered!");
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String line;

                if((line = in.readLine()) != null){
                    LOGGER.info(line);

                    String splitted[] = line.split(":");

                    if(Objects.equals(splitted[0], "preventCollision")){
                        preventCollision = Boolean.parseBoolean(splitted[1]);
                    }else if(Objects.equals(splitted[0], "autoPilot")){

                    }
                }
            }
        } catch (IOException e) {
           LOGGER.error("[SERVER]\t\t\tError occurred while running the server: " + e.getMessage());
        } catch (ConcurrentModificationException e){
            LOGGER.error("[SERVER]\t\t\t" + e.getMessage());
        }
    }

    public boolean SendCommandToClient(Command command) throws IOException, InterruptedException {
        IsStillConnected();

        Socket s = TetermineRightSocketMbot(command);

        if(s == null){
            LOGGER.info("[SERVER]\t\t\tSocket is null!");
            return false;
        }

        try {
            //INFO WHEN CONNECTED SOCKETS INCREASE
            if(prevSize == 0 || prevSize != connectedSockets.size()){
                LOGGER.info("[SERVER]\t\t\t" + String.valueOf("Connected Devices: " + connectedSockets.size()));
                prevSize = connectedSockets.size();
            }

            stream = s.getOutputStream();

            if(GetPreventCollision()){
                command = new Command("ANTISUICIDE_ON" , command.getSocket());
                stream.write(command.getName().getBytes(StandardCharsets.UTF_8));
            }else if(!GetPreventCollision()){
                command = new Command("ANTISUICIDE_OFF", command.getSocket());
                stream.write(command.getName().getBytes(StandardCharsets.UTF_8));
            }

            stream.write(command.getName().getBytes(StandardCharsets.UTF_8));
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

    public boolean SendSensorDataToClient(MbotEntity m) throws NullPointerException, InterruptedException, IOException {
        lastPackage = m.ToMbotDTO();

        String clientSocketString = "";
        if(BroadcastServer.getClientSocket() == null){
            return false;
        }

        clientSocketString = BroadcastServer.getClientSocket().split(":")[0];
        Socket s = new Socket();
        IsStillConnected();

        try{
            s = TetermineRightSocketClient(clientSocketString);

            //check for null
            if(s == null){
                if(counter > 25){
                    LOGGER.info("[SERVER]\t\t\tThere was no client registered!");
                    counter = 0;
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
            ).toMbotEntity();

            stream.write(mapper.writeValueAsBytes(m));
            stream.flush();

        } catch (IOException e) {
            stream = TetermineRightSocketClient(BroadcastServer.getClientSocket()).getOutputStream();

            stream.write(mapper.writeValueAsBytes(m));
            stream.flush();

            BroadcastServer.ResetClient();
            connectedSockets.remove(s);

            LOGGER.error("[SERVER]\t\t\t Error sending data to client (SensordatatTOClient): " + e.getMessage());
            LOGGER.error(e.toString());
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

        //CHECK IF ALL SOCKETS ARE STILL ALIVE
        IsStillConnected();

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
            return null;
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

    private Socket TetermineRightSocketClient(String address){
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

    public static Server getServer(){
        if(INSTANCE == null){
            INSTANCE = new Server();
        }

        return INSTANCE;
    }

    public static boolean GetAutoPilot(){
        return autoPilot;
    }


    public static boolean GetPreventCollision(){
        return preventCollision;
    }

    public static MbotDTO GetLastPackage(){
        return lastPackage;
    }
}