package com.mauamott.loginuser.service.impl;
import com.mauamott.loginuser.documents.User;
import com.mauamott.loginuser.dto.CreateUserDTO;
import com.mauamott.loginuser.dto.Login2FADTO;
import com.mauamott.loginuser.dto.LoginDTO;
import com.mauamott.loginuser.dto.TokenDTO;
import static com.mauamott.loginuser.exception.AuthExceptions.*;
import static com.mauamott.loginuser.utils.Constants.*;

import static com.mauamott.loginuser.exception.UserExceptions.*;
import com.mauamott.loginuser.repository.UserRepository;
import com.mauamott.loginuser.enums.Role;
import com.mauamott.loginuser.security.jwt.JwtProvider;
import com.mauamott.loginuser.service.AuthService;
import com.mauamott.loginuser.service.EmailService;
import com.mauamott.loginuser.utils.GoogleAuthenticatorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuthenticatorUtils authenticator;
    private final EmailService emailService;
    @Override
    public Mono<TokenDTO> login(LoginDTO dto) {
        return userRepository.findByUsernameOrEmail(dto.getUsername(), dto.getUsername())
                .filter(user -> passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                .flatMap(user -> {
                    if (user.isVerified()) {
                            return Mono.just(new TokenDTO(jwtProvider.generateTokenTemporary(user,TWO_HOURS)));
                    } else {
                        return Mono.error(new UserNotVerifiedException(USER_NOT_VERIFIED));
                    }
                })
                .switchIfEmpty(Mono.error(new BadCredentialsException(BAD_CREDENTIALS)));
    }

    @Override
    public Mono<Boolean> createUser(CreateUserDTO dto) {
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        // Generar el secreto para 2FA
        String secretKey = authenticator.generateSecretKey();

        // Generar URL del código QR
        String issuer = "Login2FA";
        String accountName = dto.getUsername();
        String qrCodeUri = authenticator.generateQrCodeUri(issuer, accountName, secretKey);

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(encodedPassword)
                .name(dto.getName())
                .lastname(dto.getLastname())
                .roles(dto.getRole())
                .secret(secretKey)
                .qrCodeUri(qrCodeUri)
                .build();

        return validateAndSaveUser(user)
                .flatMap(success -> {
                    if (success) {
                        return Mono.just(true);
                    } else {
                        return Mono.error(new EmailSendException(EMAIL_SEND_ERROR));
                    }
                })
                .onErrorResume(this::handleDuplicateKeyError);
    }

    @Override
    public Mono<TokenDTO> login2Fa(Login2FADTO dto) {
        String username = jwtProvider.getSubject(dto.getToken());
        return userRepository.findByUsernameOrEmail(username, username)
                .switchIfEmpty(Mono.error(new UserNotFoundException(USER_NOT_FOUND)))
                .filter(user -> jwtProvider.validate(dto.getToken()))
                .flatMap(user -> {
                    if (!user.isVerified()) {
                        String secretKey = user.getSecret();
                        int totpCode = Integer.parseInt(dto.getCode());

                        if (authenticator.isTotpValid(secretKey, totpCode)) {
                            return Mono.just(new TokenDTO(jwtProvider.generateTokenTemporary(user,TWO_HOURS)));
                        } else {
                            return Mono.error(new InvalidTotpException(INVALID_TOTP));
                        }
                    } else {
                        return Mono.error(new UserNotVerifiedException(USER_NOT_VERIFIED));
                    }
                })
                .switchIfEmpty(Mono.error(new BadCredentialsException(BAD_CREDENTIALS)));
    }

    @Override
    public Mono<Boolean> validationTokenEmail(String username, String totpCode) {
        return userRepository.findByUsernameOrEmail(username, username)
                .flatMap(user -> {
                    String secretKey = user.getSecret();
                    int code = Integer.parseInt(totpCode);

                    if (authenticator.isTotpValid(secretKey, code)) {
                        user.setVerified(true);
                        return userRepository.save(user)
                                .map(savedUser -> true)
                                .onErrorResume(this::handleDuplicateKeyError);
                    } else {
                        return Mono.error(new InvalidTotpException(INVALID_TOTP));
                    }
                })
                .switchIfEmpty(Mono.error(new UserNotFoundException(USER_NOT_FOUND)));
    }




    public Mono<Boolean> validateAndSaveUser(User user) {
        if (isValidRole(user.getRoles())) {
            return userRepository.save(user)
                    .flatMap(savedUser -> emailService.mailSenderVerification(
                                    savedUser.getEmail(),
                                    savedUser.getUsername(),
                                    savedUser.getQrCodeUri())
                            .onErrorResume(error -> {
                                // Manejar el error de envío de correo
                                log.error("Error al enviar el correo electrónico", error);
                                return Mono.just(false);
                            }))
                    .flatMap(success -> {
                        if (success) {
                            return Mono.just(true); // Si el envío de correo es exitoso, retorna true
                        } else {
                            return Mono.error(new EmailSendException(EMAIL_SEND_ERROR)); // Si falla el envío de correo, lanza una excepción
                        }
                    })
                    .onErrorResume(this::handleDuplicateKeyError); // Manejar cualquier error de duplicidad de clave
        } else {
            return Mono.error(new InvalidRoleException(INVALID_ROLE));
        }
    }


    private boolean isValidRole(String role) {
        return Role.ROLE_ADMIN.name().equals(role) || Role.ROLE_USER.name().equals(role);
    }
    public Mono<Boolean> handleDuplicateKeyError(Throwable e) {
        log.error("Error handling duplicate key", e);
        if (e instanceof DuplicateKeyException duplicateKeyException) {
            String errorMessage = duplicateKeyException.getMessage();

            if (errorMessage.contains("email")) {
                return Mono.error(new DuplicateEmailException(EMAIL_EXIST));
            } else if (errorMessage.contains("username")) {
                return Mono.error(new DuplicateUsernameException(USERNAME_EXIST));
            }
        }
        return Mono.error(e);
    }

}
