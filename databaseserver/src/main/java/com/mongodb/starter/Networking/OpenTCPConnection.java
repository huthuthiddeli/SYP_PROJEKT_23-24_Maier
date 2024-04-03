package com.mongodb.starter.Networking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.starter.models.Command;
import com.mongodb.starter.models.MbotEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class OpenTCPConnection {
    private ArrayList<Socket> activeMbots = new ArrayList<>();
    private static Logger LOGGER = LoggerFactory.getLogger(OpenTCPConnection.class);
    private ObjectMapper mapper = new ObjectMapper();
    private Socket client;


    /**
     * <p> Connects to the single client that answered the broadcast</p>
     * @return Returns a boolean that represents the status of the action
     * @throws IOException
     */
    public boolean connectTOClient() throws IOException {

        if(BroadcastServer.getClientSockets().isEmpty()) {
            return false;
        }

        if(client.isConnected()){
            return true;
        }

        String clientIP = BroadcastServer.getClientSockets();
        String[] splitted = clientIP.split(":");

        client = new Socket(splitted[0], Integer.parseInt(splitted[1]));

        return true;
    }

    /**
     * <p> Conncets via TCP to the Mbots that awnsered the Broadcast </p>
     * @return Returns a boolean that represents the status of the action
     * @throws IOException
     */
    public boolean connectToMbot() throws IOException {
        ArrayList<String> items = BroadcastServer.getMbotSockets();

        if(items.isEmpty()){
            return false;
        }

        for(String s : items){
            String[] splitted = s.split(":");

            Socket socket = new Socket(splitted[0], Integer.valueOf(splitted[1]));

            if(socket.isConnected()){
                activeMbots.add(socket);
            }
        }

        return true;
    }


    /**
    * <p>If called sends the mbot sensordata to the client, but only if it is connected!<p>
    * @param m Sensordata from mbot
    * @return returns true if it worked otherwise the returncode will be false
    * @exception IOException
    *
    **/
    public boolean sendPackageToClient(MbotEntity m) throws IOException {
        if(client == null) {
            return false;
        }

        OutputStream s = client.getOutputStream();
        String json = mapper.writeValueAsString(m);

        s.write(json.getBytes());
        LOGGER.info(json); 
        s.flush();

        return true;
    }


    /**
     * <p>
     *  Function that sends the Command object to the Mbots
     * </p>
     *
     * @param command Object that contains the command and will be transmitted
     * @param socket String that contains the ip and port of the Mbot
     * @return returns a boolean that represents the status
     * @throws IOException
    */
    public String sendPackageToMbot(Command command) throws IOException {

        String[] splitted = command.getSocket().split(":");


        for(Socket curSock : activeMbots){
            InetSocketAddress remoteAddress = (InetSocketAddress) curSock.getRemoteSocketAddress();
            if(remoteAddress.getAddress().getAddress().equals(splitted[0]) && remoteAddress.getPort() == Integer.valueOf(splitted[1])){
                OutputStream outputStream = curSock.getOutputStream();
                String json = mapper.writeValueAsString(command);
                outputStream.write(json.getBytes());
                outputStream.flush();


                return json;
            }
        }


        return null;
    }

    /**
     *
     * @return Socket
     */

    public Socket getSocket(){
        return this.client;
    }
}
