package com.mauamott.loginuser.handlers.impl;

import com.mauamott.loginuser.documents.User;
import com.mauamott.loginuser.dto.CreateUserDTO;
import com.mauamott.loginuser.dto.LoginDTO;
import com.mauamott.loginuser.dto.TokenDTO;
import com.mauamott.loginuser.handlers.AuthHandler;
import com.mauamott.loginuser.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AuthHandlerImpl implements AuthHandler {
    private final AuthServiceImpl authService;

    @Override
    public Mono<ServerResponse> login(ServerRequest request){
        Mono<LoginDTO> dtoMono = request.bodyToMono(LoginDTO.class);
        return dtoMono
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(authService.login(dto), TokenDTO.class));
    }

    @Override
    public Mono<ServerResponse> createUser(ServerRequest request){
        Mono<CreateUserDTO> dtoMono = request.bodyToMono(CreateUserDTO.class);
        return dtoMono
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(authService.createUser(dto), User.class));
    }
}
