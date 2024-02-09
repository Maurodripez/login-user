package com.mauamott.loginuser.service.impl;

import com.mauamott.loginuser.repository.UserRepository;
import com.mauamott.loginuser.security.jwt.JwtProvider;
import com.mauamott.loginuser.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;

import static com.mauamott.loginuser.utils.Constants.TWENTY_FOUR_HOURS;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JwtProvider jwtProvider;
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final TemplateEngine templateEngine;

    @Value("${correo.remitente}")
    private String remitente;

    @Override
    public Mono<Boolean> mailSenderVerification(String destinatario, String username, String qrCode) {
        MimeMessage mensaje = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mensaje,true);
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("Verificacion de correo electronico y 2FA");
            String linkVerification = "http://localhost:3000/verification-email?token="
                    +jwtProvider.generateTokenWithUsername(username,TWENTY_FOUR_HOURS)
                    +"&qrCode="+qrCode;
            String contenidoPlantilla = cargarContenidoPlantilla(username,linkVerification);
            helper.setText(contenidoPlantilla, true);

            javaMailSender.send(mensaje);
            return Mono.just(true);
        } catch (MessagingException e) {
            return Mono.just(false);
        }
    }

    @Override
    public boolean resendValidationEmail(String username) {
        return false;
    }

    public String cargarContenidoPlantilla(String nombreUsuario, String enlaceVerificacion) {
        // Crear un contexto de Thymeleaf y agregar las variables necesarias para la plantilla
        Context context = new Context();
        context.setVariable("nombreUsuario", nombreUsuario);
        context.setVariable("enlaceVerificacion", enlaceVerificacion);

        // Procesar la plantilla Thymeleaf con los datos del usuario
        return templateEngine.process("correo", context);
    }

}
