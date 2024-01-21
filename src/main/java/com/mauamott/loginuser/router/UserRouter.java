package com.mauamott.loginuser.router;

import com.mauamott.loginuser.handlers.UserHandler;
import com.mauamott.loginuser.handlers.impl.UserHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class UserRouter {
    private final String PATH = "private";
    @Bean
    public RouterFunction<ServerResponse> userRouterFunction(UserHandler userHandler){
        return RouterFunctions.route(GET("public/user/{id}").and(accept(MediaType.APPLICATION_JSON)),
                userHandler :: getUser)
                .andRoute(DELETE("/delete/user/{id}").and(accept(MediaType.APPLICATION_JSON)),
                        userHandler :: deleteUser)
                .andRoute(PUT("/update/user/{id}").and(accept(MediaType.APPLICATION_JSON)),
                        userHandler :: updateUser)
                .andRoute(POST("/change/password/{id}").and(accept(MediaType.APPLICATION_JSON)),
                        userHandler :: changePassword);
    }
}
