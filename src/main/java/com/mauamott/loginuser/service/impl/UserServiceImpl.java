package com.mauamott.loginuser.service.impl;

import com.mauamott.loginuser.documents.User;
import com.mauamott.loginuser.dto.ChangePassword;
import com.mauamott.loginuser.enums.Role;
import com.mauamott.loginuser.exception.UserExceptions;
import com.mauamott.loginuser.handlers.AuditHandler;
import com.mauamott.loginuser.repository.UserRepository;
import com.mauamott.loginuser.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.crypto.KeyGenerator;

import java.security.Key;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditHandler auditHandler;

    @Override
    public Mono<User> getUser(String id) {
        return userRepository.findById(id);
    }

    @Override
    public Mono<Boolean> deleteUser(String id) {
        return userRepository.findById(id)
                .flatMap(user -> userRepository.deleteById(id).thenReturn(true))
                .switchIfEmpty(Mono.just(false));
    }

    @Override
    public Mono<User> updateUser(String id, Mono<User> updatedUserMono) {
        return userRepository.findById(id)
                .zipWith(updatedUserMono, (existingUser, updatedUser) -> {
                    existingUser.setUsername(updatedUser.getUsername());
                    existingUser.setEmail(updatedUser.getEmail());
                    existingUser.setName(updatedUser.getName());
                    existingUser.setLastname(updatedUser.getLastname());
                    existingUser.setRoles(updatedUser.getRoles());
                    return existingUser;
                })
                .flatMap(userRepository::save);
    }

    @Override
    public Mono<ServerResponse> changePassword(String id,Mono<User> existingUserMono, Mono<ChangePassword> updatedUserMono) {

        return Mono.zip(existingUserMono, updatedUserMono, (existingUser, updatedUser) -> {
                    if (!passwordEncoder.matches(updatedUser.getOldPassword(), existingUser.getPassword())) {
                        throw new UserExceptions.IncorrectPasswordException("Old password is incorrect");
                    }
                    String passwordEncode = passwordEncoder.encode(updatedUser.getPassword());
                    existingUser.setPassword(passwordEncode);
                    return existingUser;
                })
                .flatMap(updatedUser -> {
                    return Mono.zip(
                                    auditHandler.saveAuditLog(updatedUser.getId(), "CHANGE_PASSWORD"),
                                    userRepository.save(updatedUser)
                            )
                            .map(Tuple2::getT2);
                })
                .flatMap(updatedUser -> ServerResponse.ok().build())
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(UserExceptions.IncorrectPasswordException.class, ex -> {
                    return auditHandler.saveAuditLog(id, "PASSWORD_MISMATCH")
                            .then(Mono.error(ex));
                })
                .onErrorResume(this::handleUpdateError);
    }


    private Mono<ServerResponse> handleUpdateError(Throwable e) {
        log.error("Error handling user update", e);
        if (e instanceof DuplicateKeyException duplicateKeyException) {
            String errorMessage = duplicateKeyException.getMessage();

            if (errorMessage.contains("email")) {
                return Mono.error(new UserExceptions.DuplicateEmailException("Email already exists"));
            } else if (errorMessage.contains("username")) {
                return Mono.error(new UserExceptions.DuplicateUsernameException("Username already exists"));
            }
        }
        return Mono.error(e);
    }


    public Mono<User> handleDuplicateKeyError(Throwable e) {
        log.error("Error handling duplicate key", e);
        if (e instanceof DuplicateKeyException duplicateKeyException) {
            String errorMessage = duplicateKeyException.getMessage();

            if (errorMessage.contains("email")) {
                return Mono.error(new UserExceptions.DuplicateEmailException("Email already exists"));
            } else if (errorMessage.contains("username")) {
                return Mono.error(new UserExceptions.DuplicateUsernameException("Username already exists"));
            }
        }
        return Mono.error(e);
    }

}
