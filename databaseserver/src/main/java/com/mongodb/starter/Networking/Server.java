package com.mongodb.starter.Networking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private static Server INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private ArrayList<Command> commandList = new ArrayList<>();
    private ArrayList<String> debugList = new ArrayList<>(List.of("1;1", "50;-50", "25;25", "299;299", "-90;90", "0;0"));

    private ArrayList<Socket> connectedSockets = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    private int prevSize = 0;
    private OutputStream outputStream;


    private Server(){}

    public void RUN() {
        int portNumber = 5000;

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            LOGGER.info("[SERVER]\t\t\tTCP-Server started on port " + portNumber);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept incoming client connections

                LOGGER.info("[SERVER]\t\t\tMbot connected: " + clientSocket.getInetAddress().getHostAddress());

                //check if ip address is already registered
                if(IsRegisteredSocket(clientSocket)){
                    continue;
                }

                //SEND DATA TO MBOT (DELETE LATER)
                for(InetAddress socket : BroadcastServer.getMbotSockets()){
                    if(Objects.equals(clientSocket.getInetAddress(), socket)){
                        OutputStream s = clientSocket.getOutputStream();

                        while(!debugList.isEmpty()){
                            if(!clientSocket.isConnected()){
                                connectedSockets.remove(clientSocket);
                            }

                            //TEST DATA
                            //s.write(debugList.get(0).getBytes());
                            //LOGGER.info("[SERVER]\t\tItem sent to MBOT: " + debugList.get(0));

                            debugList.remove(0);
                            Thread.sleep(2000);
                        }

                        s.flush();
                    }
                }


                //SEND DATA TO CLIENT (DELETE LATER)
                if(Objects.equals(clientSocket.getInetAddress(), BroadcastServer.getClientSocket())){
                    OutputStream s = clientSocket.getOutputStream();
                    /*
                    MbotDTO mbotDTO = new MbotDTO(2.5f, new ArrayList<Integer>(Arrays.asList(1,2,3,6,8,9,99)), 3,
                            new ArrayList<Integer>(Arrays.asList(1,2,3,4,6))
                            , 95, 22, "1.12.23.4");

                    s.write(mapper.writeValueAsBytes(mbotDTO));

                     */

                    s.write(mapper.writeValueAsBytes(BroadcastServer.getMbotSockets()));

                    s.flush();
                }

            }
        } catch (IOException e) {
           LOGGER.error("[SERVER]\t\t\tError occurred while running the server: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ConcurrentModificationException e){
            LOGGER.error("[SERVER]\t\t\t" + e.getMessage());
        }catch(Exception e){
            LOGGER.error("[SERVER]\t\t\tError: " + e.getMessage());
        }
    }

    public boolean SendCommandToClient(Command command) throws IOException, InterruptedException {
        Socket s = TetermineRightSocket(command);

        if(s == null){
            return false;
        }

        LOGGER.info("[SERVER]\t\t\tCommand: " + command.toString());

        //TODO: An established conection was aborted by the software in your hostmachine
        //RANDOMLY DISCONNECTS FROM MBOT (CONNECTION CLOSE BY PEER)

        try {
            //INFO WHEN CONNECTED SOCKETS INCREASE
            if(prevSize == 0 || prevSize != connectedSockets.size()){
                LOGGER.info("[SERVER]\t\t\t" + String.valueOf("Size of List: " + connectedSockets.size()));
                prevSize = connectedSockets.size();
            }

            //DELETE IF THE CONNECTION IS NOT UP ANYMORE
            if(!s.isConnected()){
                connectedSockets.remove(s);
            }
                outputStream = s.getOutputStream();
                outputStream.write(command.getName().getBytes());

        }catch (Exception ex){
            LOGGER.error("[SERVER]\t\t\t" + ex.getMessage());
            LOGGER.error("[SERVER]\t\t\tRestart Mbot with ip: " + command.getSocket());
            return false;
        }



        return true;
    }

    private boolean IsConnected(String clientSocket, String listSocket){
        if(Objects.equals(clientSocket,listSocket)){
            return true;
        }

        return false;
    }


    //IF IP IS ALREADY IN LIST SKIP ACTIONS
    private boolean IsRegisteredSocket(Socket address){
        if(address == null){
            return false;
        }


        for(Socket s : connectedSockets){
            //LOGGER.info(String.valueOf("[SERVER]\t\t\t" + s.getInetAddress() + "  " + address.getInetAddress()));
            if(Objects.equals(s.getInetAddress(), address.getInetAddress())){
                return true;
            }

            connectedSockets.add(address);
        }

        if(connectedSockets.isEmpty()){
            connectedSockets.add(address);
        }

        return false;
    }

    private boolean IsRegisteredString(String socket){
        for(Socket s : connectedSockets) {
            //LOGGER.info(String.valueOf("[SERVER]\t\t\t" + s.getInetAddress() + "  " + socket));
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

    private Socket TetermineRightSocket(Command command){

        for(int i = 0; i < connectedSockets.size(); i++){
            if(IsRegisteredString(command.getSocket())){
                return connectedSockets.get(i);
            }
        }

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