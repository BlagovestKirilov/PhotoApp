package com.example.photoapp.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Response {
    private String message;

    public Response(String message) {
        this.message = message;
    }
}