package com.mongodb.starter.Networking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.starter.models.MbotEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class OpenTCPConnection {
    private ArrayList<Socket> activeSockets = new ArrayList<>();
    private static Logger LOGGER = LoggerFactory.getLogger(OpenTCPConnection.class);
    private ObjectMapper mapper = new ObjectMapper();
    private Socket client;



    public void connectTOClient() throws IOException {

        if(BroadcastServer.getClientSockets().length() == 0)
            return;

        String clientIP = BroadcastServer.getClientSockets();
        String[] splitted = clientIP.split(":");

        client = new Socket(splitted[0], Integer.valueOf(splitted[1]));
    }

    public boolean sendPackageToClient(MbotEntity m) throws IOException {
        if(client == null)
            return false;

        OutputStream s = client.getOutputStream();

        String json = mapper.writeValueAsString(m);

        s.write(json.getBytes());
        LOGGER.info(json); 
        s.flush();

        return true;
    }

    public void sendMessage(String s){
        activeSockets.forEach((e) ->{
            try {
                PrintWriter out = new PrintWriter(e.getOutputStream(), true);

                out.println(s);

                out.close();
                LOGGER.info("Command has been sent!");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public Socket getSocket(){
        return this.client;
    }
}
