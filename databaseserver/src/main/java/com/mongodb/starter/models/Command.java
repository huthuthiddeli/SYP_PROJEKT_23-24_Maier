package com.mongodb.starter.models;

public class Command {

    //Refactor: update command to fitting class
    private String name;
    private String socket;


    public Command(String name, String socket) {
        this.name = name;
        this.socket = socket;
    }

    @Override
    public String toString() {
        return "Command{" +
                "name='" + name + '\'' +
                ", socket='" + socket +'\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getSocket() {
        return socket;
    }
}
