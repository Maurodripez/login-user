package com.mauamott.loginuser.handlers.impl;

import com.mauamott.loginuser.documents.User;
import com.mauamott.loginuser.dto.CreateUserDTO;
import com.mauamott.loginuser.dto.Login2FADTO;
import com.mauamott.loginuser.dto.LoginDTO;
import com.mauamott.loginuser.dto.TokenDTO;
import com.mauamott.loginuser.exception.AuthExceptions;
import com.mauamott.loginuser.exception.JwtExceptions.BadTokenException;
import com.mauamott.loginuser.exception.UserExceptions;
import com.mauamott.loginuser.handlers.AuthHandler;
import com.mauamott.loginuser.security.jwt.JwtProvider;
import com.mauamott.loginuser.service.AuthService;
import com.mauamott.loginuser.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.mauamott.loginuser.utils.Constants.TWO_HOURS;
import static com.mauamott.loginuser.utils.Constants.USER_NOT_VERIFIED;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AuthHandlerImpl implements AuthHandler {
    private final AuthService authService;
    private final JwtProvider jwtProvider;

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

    @Override
    public Mono<ServerResponse> login2FA(ServerRequest request) {
        Mono<Login2FADTO> dtoMono = request.bodyToMono(Login2FADTO.class);
        return dtoMono
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(authService.login2Fa(dto), TokenDTO.class));
    }

    @Override
    public Mono<ServerResponse> validationTokenEmail(ServerRequest request) {
        String token = request.queryParam("token").orElse("");
        String totpCode = request.queryParam("totpCode").orElse("");

        boolean isValid = jwtProvider.validate(token);

        return Mono.just(isValid)
                .flatMap(valid -> {
                    if (valid) {
                        String username = jwtProvider.getSubject(token);
                        return authService.validationTokenEmail(username, totpCode)
                                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                                .onErrorResume(error -> {
                                    // Manejar el error que pueda lanzar el servicio
                                    if (error instanceof UserExceptions.UserNotFoundException) {
                                        return ServerResponse.status(HttpStatus.NOT_FOUND)
                                                .bodyValue("Usuario no encontrado");
                                    } else if (error instanceof AuthExceptions.InvalidTotpException) {
                                        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                                                .bodyValue("Totp inválido");
                                    } else {
                                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .bodyValue("Error interno del servidor");
                                    }
                                });
                    } else {
                        return Mono.error(new BadTokenException(USER_NOT_VERIFIED));
                    }
                });
    }

}
