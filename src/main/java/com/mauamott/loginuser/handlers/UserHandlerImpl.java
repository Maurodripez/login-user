package com.mauamott.loginuser.handlers;

import com.mauamott.loginuser.documents.User;
import static com.mauamott.loginuser.exception.UserExceptions.*;

import com.mauamott.loginuser.models.Role;
import com.mauamott.loginuser.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Slf4j
@Component
public class UserHandlerImpl implements UserHandler {

    private final UserRepository userRepository;

    private final AuditHandler auditHandler;

    public UserHandlerImpl(UserRepository userRepository, AuditHandler auditHandler) {
        this.userRepository = userRepository;
        this.auditHandler = auditHandler;
    }

    @Override
    public Mono<ServerResponse> getUsers(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return userRepository.findById(id)
                .flatMap(user -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(user))
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    @Override
    public Mono<ServerResponse> saveUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(User.class)
                .flatMap(user -> {
                    // Copiar la contraseña actual a oldPassword antes de guardar
                    user.setOldPassword(user.getPassword());
                    return validateAndSaveUser(user);
                })
                .flatMap(savedUser -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(savedUser))
                );
    }


    @Override
    public Mono<ServerResponse> deleteUser(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");

        return userRepository.findById(id)
                .flatMap(user -> userRepository.deleteById(id)
                        .then(ServerResponse.ok().build())
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    @Override
    public Mono<ServerResponse> updateUser(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        Mono<User> updatedUserMono = serverRequest.bodyToMono(User.class);

        return userRepository.findById(id)
                .zipWith(updatedUserMono, (existingUser, updatedUser) -> {
                    existingUser.setUsername(updatedUser.getUsername());
                    existingUser.setEmail(updatedUser.getEmail());
                    existingUser.setName(updatedUser.getName());
                    existingUser.setLastname(updatedUser.getLastname());
                    existingUser.setRoles(updatedUser.getRoles());
                    return existingUser;
                })
                .flatMap(userRepository::save)
                .flatMap(updatedUser -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(updatedUser))
                )
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(this::handleUpdateError);
    }

    @Override
    public Mono<ServerResponse> changePassword(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");

        Mono<User> existingUserMono = userRepository.findById(id);
        Mono<User> updatedUserMono = serverRequest.bodyToMono(User.class);

        return Mono.zip(existingUserMono, updatedUserMono, (existingUser, updatedUser) -> {
                    if (!existingUser.getPassword().equals(updatedUser.getOldPassword())) {
                        throw new IncorrectPasswordException("Old password is incorrect");
                    }
                    existingUser.setPassword(updatedUser.getPassword());
                    existingUser.setOldPassword(updatedUser.getPassword());
                    return existingUser;
                })
                .flatMap(updatedUser -> {
                    // Llamar al método saveAuditLog para registrar el evento de cambio de contraseña
                    return Mono.zip(
                                    auditHandler.saveAuditLog(updatedUser.getId(), "CHANGE_PASSWORD"),
                                    userRepository.save(updatedUser)
                            )
                            .map(Tuple2::getT2);  // Tomar solo el resultado del userRepository.save
                })
                .flatMap(updatedUser -> ServerResponse.ok().build())
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(IncorrectPasswordException.class, ex -> {
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
                return Mono.error(new DuplicateEmailException("Email already exists"));
            } else if (errorMessage.contains("username")) {
                return Mono.error(new DuplicateUsernameException("Username already exists"));
            }
        }
        return Mono.error(e);
    }

    public Mono<User> validateAndSaveUser(User user) {
        if (isValidRole(user.getRoles())) {
            return userRepository.save(user)
                    .onErrorResume(this::handleDuplicateKeyError);
        } else {
            return Mono.error(new InvalidRoleException("Invalid user role"));
        }
    }

    public Mono<User> handleDuplicateKeyError(Throwable e) {
        log.error("Error handling duplicate key", e);
        if (e instanceof DuplicateKeyException duplicateKeyException) {
            String errorMessage = duplicateKeyException.getMessage();

            if (errorMessage.contains("email")) {
                return Mono.error(new DuplicateEmailException("Email already exists"));
            } else if (errorMessage.contains("username")) {
                return Mono.error(new DuplicateUsernameException("Username already exists"));
            }
        }
        return Mono.error(e);
    }
    
    private boolean isValidRole(String role) {
        return Role.ADMIN.name().equals(role) || Role.INVITED.name().equals(role);
    }
}
