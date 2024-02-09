package com.mauamott.loginuser.service;

import reactor.core.publisher.Mono;

public interface EmailService {
    Mono<Boolean> mailSenderVerification(String destinatario, String nombreUsuario, String qrCode);

    boolean resendValidationEmail(String username);
}
