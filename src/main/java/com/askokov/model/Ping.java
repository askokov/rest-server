package com.askokov.model;

public class Ping {
    public static final String OK = "OK";

    private String name;

    public Ping(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
