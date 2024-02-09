package com.mauamott.loginuser.router;

import com.mauamott.loginuser.handlers.AuthHandler;
import com.mauamott.loginuser.handlers.impl.AuthHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class AuthRouter {
    @Bean
    public RouterFunction<ServerResponse> authRouterFunction(AuthHandler authHandler){
        String PATH = "auth/";
        return RouterFunctions.route(POST(PATH + "login").and(accept(MediaType.APPLICATION_JSON)),
                        authHandler :: login)
                .andRoute(POST(PATH + "createUser").and(accept(MediaType.APPLICATION_JSON)),
                        authHandler :: createUser)
                .andRoute(POST(PATH + "login2FA").and(accept(MediaType.APPLICATION_JSON)),
                authHandler :: login2FA)
                .andRoute(GET(PATH + "validationEmail").and(accept(MediaType.APPLICATION_JSON)),
                        authHandler :: validationTokenEmail);
    }
}
