package com.mauamott.loginuser.security.jwt;

import static com.mauamott.loginuser.exception.JwtExceptions.*;
import static com.mauamott.loginuser.utils.Constants.INVALID_AUTH;
import static com.mauamott.loginuser.utils.Constants.NO_TOKEN_FOUND;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        if(path.contains("auth"))
            return chain.filter(exchange);
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(auth == null)
            return Mono.error(new NoTokenWasFoundException(NO_TOKEN_FOUND));
        if(!auth.startsWith("Bearer "))
            return Mono.error(new InvalidAuthException(INVALID_AUTH));
        String token = auth.replace("Bearer ","");
        exchange.getAttributes().put("token",token);
        return chain.filter(exchange);
    }
}
