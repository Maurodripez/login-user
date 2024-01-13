package com.mauamott.loginuser.router;

import com.mauamott.loginuser.handlers.UserHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class UserRouter {

    @Bean
    public RouterFunction<ServerResponse> userRouterFunction(UserHandlerImpl userHandler){
        return RouterFunctions.route(GET("/login/user/{id}").and(accept(MediaType.APPLICATION_JSON)),
                        userHandler :: getUsers)
                .andRoute(POST("/login/user").and(accept(MediaType.APPLICATION_JSON)),
                        userHandler :: saveUser)
                .andRoute(DELETE("/login/delete/user/{id}").and(accept(MediaType.APPLICATION_JSON)),
                        userHandler :: deleteUser)
                .andRoute(PUT("/login/update/user/{id}").and(accept(MediaType.APPLICATION_JSON)),
                        userHandler :: updateUser)
                .andRoute(POST("/login/change/password/{id}").and(accept(MediaType.APPLICATION_JSON)),
                        userHandler :: changePassword);
    }
}
