package com.mauamott.loginuser.service.impl;
import com.mauamott.loginuser.documents.User;
import com.mauamott.loginuser.dto.CreateUserDTO;
import com.mauamott.loginuser.dto.LoginDTO;
import com.mauamott.loginuser.dto.TokenDTO;
import com.mauamott.loginuser.exception.UserExceptions;
import com.mauamott.loginuser.repository.UserRepository;
import com.mauamott.loginuser.enums.Role;
import com.mauamott.loginuser.security.jwt.JwtProvider;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public Mono<TokenDTO> login(LoginDTO dto) {
        return userRepository.findByUsernameOrEmail(dto.getUsername(), dto.getUsername())
                .filter(user -> passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                .flatMap(user -> {
                    if (!user.isVerify()) {
                        // User has 2FA enabled, validate TOTP
                        String secretKey = user.getSecret();
                        int totpCode = Integer.parseInt(dto.getCode());

                        if (isTotpValid(secretKey, totpCode)) {
                            return Mono.just(new TokenDTO(jwtProvider.generateToken(user)));
                        } else {
                            return Mono.error(new Exception("Invalid TOTP"));
                        }
                    } else {
                        // User doesn't have 2FA enabled
                        return Mono.just(new TokenDTO(jwtProvider.generateToken(user)));
                    }
                })
                .switchIfEmpty(Mono.error(new Exception("Bad credentials")));
    }
    private boolean isTotpValid(String secretKey, int totpCode) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        System.out.println("Secret Key: " + secretKey);
        System.out.println("TOTP Code: " + totpCode);

        boolean isValid = gAuth.authorize(secretKey, totpCode);
        System.out.println("Is TOTP Valid: " + isValid);

        return isValid;
    }



    public Mono<User> createUser(CreateUserDTO dto){
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        // Generar el secreto para 2FA
        String secretKey = generateSecretKey();

        // Generar URL del código QR
        String issuer = "Login2FA";
        String accountName = dto.getUsername();
        String qrCodeUri = generateQrCodeUri(issuer, accountName, secretKey);

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(encodedPassword)
                .name(dto.getName())
                .lastname(dto.getLastname())
                .roles(dto.getRole())
                .secret(secretKey)
                .build();

        Mono<Boolean> userExist = userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail()).hasElement();

        return userExist
                .flatMap(exist -> exist ?
                        Mono.error(new Exception("Username or Email already in use"))
                        : userRepository.save(user)
                        .map(savedUser -> {
                            // Adjuntar la URL del código QR al usuario
                            savedUser.setQrCodeUri(qrCodeUri);
                            return savedUser;
                        })
                );
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

    public static String generateSecretKey() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gAuth.createCredentials();

        // Return the generated secret key
        return key.getKey();
    }

    public static String generateQrCodeUri(String issuer, String accountName, String secretKey) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, accountName, secretKey, issuer
        );
    }
}
