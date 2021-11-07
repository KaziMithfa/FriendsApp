package com.example.friendsapp;

public class Requests {

    public String request_type;
    public String id;

    public Requests() {

    }

    public Requests(String id, String request_type) {

        this.id = id;
        this.request_type = request_type;
    }

    public String getRequest_type() {
        return request_type;
    }

    public String getId() {
        return id;
    }
}
