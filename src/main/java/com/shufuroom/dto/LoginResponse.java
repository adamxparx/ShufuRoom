package com.shufuroom.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String message;
    private String token;
    private Object user;

    public LoginResponse(String message, String token, Object user) {
        this.message = message;
        this.token = token;
        this.user = user;
    }

    public String getMessage() { return message; }
    public String getToken() { return token; }
    public Object getUser() { return user; }
}
