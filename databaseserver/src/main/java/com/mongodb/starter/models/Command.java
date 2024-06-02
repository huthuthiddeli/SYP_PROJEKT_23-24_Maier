package com.mongodb.starter.models;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Command {

    //Refactor: update command to fitting class
    private String name;
    private String socket;

    public Command(){}

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
}
