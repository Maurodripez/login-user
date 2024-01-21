package com.mauamott.loginuser.service;

import com.mauamott.loginuser.documents.User;
import com.mauamott.loginuser.dto.ChangePassword;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> getUser(String id);

    Mono<Boolean> deleteUser(String id);

    Mono<User> updateUser(String id, Mono<User> user);

    Mono<ServerResponse> changePassword(String id,Mono<User> existingUserMono, Mono<ChangePassword> updatedUserMono);

}
