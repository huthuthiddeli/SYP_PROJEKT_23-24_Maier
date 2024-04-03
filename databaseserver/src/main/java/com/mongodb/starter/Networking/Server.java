package com.mongodb.starter.Networking;

import com.mongodb.starter.ApplicationStarter;
import com.mongodb.starter.models.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;

public class Server {

    private static Server INSTANCE;
    private static Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private ArrayList<Command> commandList = new ArrayList<>();
    private static ArrayList<String> debugList = new ArrayList<>(List.of("1;1", "50;-50", "25;25", "299;299", "-90;90", "0;0"));

    private static ArrayList<Socket> connectedSockets = new ArrayList<>();

    private int prevSize = 0;

    private Server(){}



    public void RUN() {
        int portNumber = 5000; // Port number on which the server will listen

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            LOGGER.info("[SERVER]\t\tTCP-Server started on port " + portNumber);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept incoming client connections


                //check if ip address is already registered
                for(Socket s : connectedSockets){
                    LOGGER.info(String.valueOf("[SERVER]\t\t" + s.getInetAddress() + "  " + clientSocket.getInetAddress()));
                    if(Objects.equals(s.getInetAddress(), clientSocket.getInetAddress())){
                        break;
                    }

                    connectedSockets.add(clientSocket);
                }

                if(connectedSockets.isEmpty()){
                    connectedSockets.add(clientSocket);
                }

                LOGGER.info("[SERVER]\t\tClient connected: " + clientSocket.getInetAddress().getHostAddress());
                for(InetAddress socket : BroadcastServer.getMbotSockets()){

                    if(Objects.equals(clientSocket.getInetAddress(), socket)){
                        OutputStream s = clientSocket.getOutputStream();
                        while(!debugList.isEmpty()){


                            if(!clientSocket.isConnected()){
                                connectedSockets.remove(clientSocket);
                                return;
                            }

                            s.write(debugList.get(0).getBytes());
                            debugList.remove(0);
                            Thread.sleep(2000);
                        }


                        s.flush();
                    }
                }
            }
        } catch (IOException e) {
           LOGGER.error("[SERVER]\t\tError occurred while running the server: " + e.getMessage());
           e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ConcurrentModificationException e){
            LOGGER.error("[SERVER]\t\t" + e.getMessage());
        }
    }

    public boolean sendCommandToClient(Command m) throws IOException {
        OutputStream s;

        //TODO: An established conection was aborted by the software in your hostmachine
        //happened when sending packages to the mbot via postman

        for(int i = 0; i < connectedSockets.size(); i++){
            if(Objects.equals(connectedSockets.get(i).getInetAddress().toString(), m.getSocket())){



                if(prevSize == 0 || prevSize != connectedSockets.size()){
                    LOGGER.info("[SERVER]\t\t" + String.valueOf("Size of List: " + connectedSockets.size()));
                    prevSize = connectedSockets.size();
                }


                if(!connectedSockets.get(i).isConnected()){
                    connectedSockets.remove(i);
                }

                try{
                    s = connectedSockets.get(i).getOutputStream();
                    s.write(m.getName().getBytes());
                    s.flush();
                }catch (Exception ex) {
                    LOGGER.error(ex.getMessage().toString());
                    LOGGER.error("[SERVER]\t\tRestart Mbot with ip: " + m.getSocket());
                    return false;
                }
            }
        }

        return true;
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