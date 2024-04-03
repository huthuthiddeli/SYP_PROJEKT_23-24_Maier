package com.mongodb.starter.models;

import java.net.InetAddress;

public class Command {
    private int id;
    private String name;
    private String socket;


    public Command(int id, String name, String socket) {
        this.id = id;
        this.name = name;
        this.socket = socket;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Command{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", socket='" + socket +'\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSocket() {
        return socket;
    }

    public void setSocket(String  socket) {
        this.socket = socket;
    }
}
