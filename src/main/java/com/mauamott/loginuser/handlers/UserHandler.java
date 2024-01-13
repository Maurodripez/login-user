package com.mauamott.loginuser.handlers;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface UserHandler {
    Mono<ServerResponse> getUsers(ServerRequest serverRequest);

    Mono<ServerResponse> saveUser(ServerRequest serverRequest);

    Mono<ServerResponse> deleteUser(ServerRequest serverRequest);

    Mono<ServerResponse> updateUser(ServerRequest serverRequest);

    Mono<ServerResponse> changePassword(ServerRequest serverRequest);
}
