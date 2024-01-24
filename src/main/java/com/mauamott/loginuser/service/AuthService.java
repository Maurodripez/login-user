package com.mauamott.loginuser.service;

import com.mauamott.loginuser.documents.User;
import com.mauamott.loginuser.dto.CreateUserDTO;
import com.mauamott.loginuser.dto.LoginDTO;
import com.mauamott.loginuser.dto.TokenDTO;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<TokenDTO> login(LoginDTO dto);
    Mono<User> createUser(CreateUserDTO dto);

    }
