package com.mongodb.starter;

import com.mongodb.starter.Networking.BroadcastServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApplicationStarter {
    public static void main(String[] args) {

        Thread newThread = new Thread(() ->{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            BroadcastServer.RUN();
        });

        newThread.start();

        SpringApplication.run(ApplicationStarter.class, args);
    }
}