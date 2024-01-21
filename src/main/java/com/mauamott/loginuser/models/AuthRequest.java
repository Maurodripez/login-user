package com.mauamott.loginuser.models;

import lombok.Data;

@Data
public class AuthRequest {

    private String username;
    private String password;
}
