package com.mauamott.loginuser.service;

import com.mauamott.loginuser.dto.CreateUserDTO;
import com.mauamott.loginuser.dto.Login2FADTO;
import com.mauamott.loginuser.dto.LoginDTO;
import com.mauamott.loginuser.dto.TokenDTO;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<TokenDTO> login(LoginDTO dto);
    Mono<Boolean> createUser(CreateUserDTO dto);

    Mono<TokenDTO> login2Fa(Login2FADTO dto);

    Mono<Boolean> validationTokenEmail(String username,String totpCode);
}
