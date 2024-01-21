package com.mauamott.loginuser.router;

import com.mauamott.loginuser.handlers.AuthHandler;
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
    private final String PATH = "auth/";
    @Bean
    public RouterFunction<ServerResponse> authRouterFunction(AuthHandler authHandler){
        return RouterFunctions.route(POST(PATH + "login").and(accept(MediaType.APPLICATION_JSON)),
                        authHandler :: login)
                .andRoute(POST(PATH + "createUser").and(accept(MediaType.APPLICATION_JSON)),
                        authHandler :: createUser);
    }
}
