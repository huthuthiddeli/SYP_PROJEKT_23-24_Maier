package com.mongodb.starter;

import com.mongodb.starter.Networking.BroadcastServer;
import com.mongodb.starter.Networking.Server;
import com.mongodb.starter.Networking.UDP_Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApplicationStarter {
    public static void main(String[] args) {

        Thread broadcast = new Thread(() ->{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            BroadcastServer.RUN();
        });

        Thread server = new Thread(() ->{
            try{
                Thread.sleep(2000);
            }catch (InterruptedException ex){
                throw new RuntimeException(ex);
            }

            Server.getServer().RUN();
        });

        Thread UDP_SERVER = new Thread(() ->{
            try{
                Thread.sleep(2000);
            }catch (InterruptedException ex){
                throw new RuntimeException(ex);
            }
            UDP_Server.GetInstance().RUN();
        });

        server.start();
        broadcast.start();
        UDP_SERVER.start();

        SpringApplication.run(ApplicationStarter.class, args);
    }
}