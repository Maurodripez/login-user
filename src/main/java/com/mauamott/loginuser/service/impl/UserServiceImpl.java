package com.mauamott.loginuser.service.impl;

import com.mauamott.loginuser.documents.User;
import com.mauamott.loginuser.dto.ChangePasswordDTO;
import static com.mauamott.loginuser.exception.UserExceptions.*;
import static com.mauamott.loginuser.utils.Constants.*;

import com.mauamott.loginuser.handlers.AuditHandler;
import com.mauamott.loginuser.repository.UserRepository;
import com.mauamott.loginuser.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

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
                .switchIfEmpty(Mono.just(false))
                .onErrorResume(ErrorDeleteUserException.class, ex -> auditHandler.saveAuditLog(id, ERROR_DELETE_USER)
                        .then(Mono.error(ex)));
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
                .flatMap(userRepository::save)
                .onErrorResume(ErrorUpdateUserException.class, ex -> auditHandler.saveAuditLog(id, ERROR_UPDATE_USER)
                        .then(Mono.error(ex)));
    }

    @Override
    public Mono<ServerResponse> changePassword(String id,Mono<User> existingUserMono, Mono<ChangePasswordDTO> updatedUserMono) {

        return Mono.zip(existingUserMono, updatedUserMono, (existingUser, updatedUser) -> {
                    if (!passwordEncoder.matches(updatedUser.getOldPassword(), existingUser.getPassword())) {
                        throw new IncorrectPasswordException(OLD_PASSWORD_INCORRECT);
                    }
                    String passwordEncode = passwordEncoder.encode(updatedUser.getPassword());
                    existingUser.setPassword(passwordEncode);
                    return existingUser;
                })
                .flatMap(updatedUser -> Mono.zip(
                                auditHandler.saveAuditLog(updatedUser.getId(), CHANGE_PASSWORD),
                                userRepository.save(updatedUser)
                        )
                        .map(Tuple2::getT2))
                .flatMap(updatedUser -> ServerResponse.ok().build())
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(IncorrectPasswordException.class, ex -> auditHandler.saveAuditLog(id, PASSWORD_MISMATCH)
                        .then(Mono.error(ex)));
    }
}
