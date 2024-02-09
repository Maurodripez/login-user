package com.mauamott.loginuser.handlers;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface AuthHandler {
    Mono<ServerResponse> login(ServerRequest request);
    Mono<ServerResponse> createUser(ServerRequest request);

    Mono<ServerResponse> login2FA(ServerRequest request);
    Mono<ServerResponse> validationTokenEmail(ServerRequest request);

}
