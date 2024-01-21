package com.mauamott.loginuser.handlers.impl;

import com.mauamott.loginuser.documents.User;
import com.mauamott.loginuser.dto.ChangePassword;
import com.mauamott.loginuser.exception.UserExceptions;
import com.mauamott.loginuser.handlers.AuditHandler;
import com.mauamott.loginuser.handlers.UserHandler;
import com.mauamott.loginuser.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class UserHandlerImpl implements UserHandler {
    private final UserService userService;
    private final AuditHandler auditHandler;

    @Override
    public Mono<ServerResponse> getUser(ServerRequest request) {
        String id = request.pathVariable("id");

        return userService.getUser(id)
                .flatMap(user -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(user))
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    @Override
    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        String id = request.pathVariable("id");

        return userService.deleteUser(id)
                .flatMap(deleted -> deleted ?
                        ServerResponse.ok().build() :
                        ServerResponse.notFound().build());
    }

    @Override
    public Mono<ServerResponse> updateUser(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<User> updatedUserMono = request.bodyToMono(User.class);

        return userService.updateUser(id, updatedUserMono)
                .flatMap(updatedUser -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(updatedUser)))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(this::handleUpdateError);
    }

    @Override
    public Mono<ServerResponse> changePassword(ServerRequest request) {
        String id = request.pathVariable("id");

        Mono<User> existingUserMono = userService.getUser(id);
        Mono<ChangePassword> updatedUserMono = request.bodyToMono(ChangePassword.class);
        return userService.changePassword(id,existingUserMono,updatedUserMono);
    }


    private Mono<ServerResponse> handleUpdateError(Throwable throwable) {
        // Manejar el error de actualización aquí si es necesario
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
