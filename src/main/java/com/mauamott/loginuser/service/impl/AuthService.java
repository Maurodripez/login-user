package com.mauamott.loginuser.service.impl;

import com.mauamott.loginuser.documents.User;
import com.mauamott.loginuser.dto.CreateUserDTO;
import com.mauamott.loginuser.dto.LoginDTO;
import com.mauamott.loginuser.dto.TokenDTO;
import com.mauamott.loginuser.exception.UserExceptions;
import com.mauamott.loginuser.repository.UserRepository;
import com.mauamott.loginuser.enums.Role;
import com.mauamott.loginuser.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public Mono<TokenDTO> login(LoginDTO dto){
        return userRepository.findByUsernameOrEmail(dto.getUsername(),dto.getUsername())
                .filter(user -> passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                .map(user ->new TokenDTO(jwtProvider.generateToken(user)))
                .switchIfEmpty(Mono.error(new Exception("Bad credentials")));
    }

    public Mono<User> createUser(CreateUserDTO dto){
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(encodedPassword)
                .name(dto.getName())
                .lastname(dto.getLastname())
                .roles(dto.getRole())
                .build();

        Mono<Boolean> userExist = userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail()).hasElement();

        return userExist
                .flatMap(exist -> exist ?
                        Mono.error(new Exception("Username or Email already in use"))
                        : userRepository.save(user));
    }

    public Mono<User> validateAndSaveUser(User user) {
        if (isValidRole(user.getRoles())) {
            return userRepository.save(user)
                    .onErrorResume(this::handleDuplicateKeyError);
        } else {
            return Mono.error(new UserExceptions.InvalidRoleException("Invalid user role"));
        }
    }

    private boolean isValidRole(String role) {
        return Role.ROLE_ADMIN.name().equals(role) || Role.ROLE_USER.name().equals(role);
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
