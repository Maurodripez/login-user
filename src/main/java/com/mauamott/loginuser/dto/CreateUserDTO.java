package com.mauamott.loginuser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateUserDTO {
    private String username;
    private String password;
    private String email;
    private boolean verify = false;
    private String name;
    private String lastname;
    private String oldPassword;
    private String role = "ROLE_USER";
}
