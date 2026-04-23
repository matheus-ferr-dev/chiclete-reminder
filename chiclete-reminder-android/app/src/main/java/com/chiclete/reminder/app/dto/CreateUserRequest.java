package com.chiclete.reminder.app.dto;

public class CreateUserRequest {
    public String name;
    public String email;
    public String password;

    public CreateUserRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
