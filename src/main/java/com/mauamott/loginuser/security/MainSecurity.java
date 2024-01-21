package com.mauamott.loginuser.security;

import com.mauamott.loginuser.security.jwt.JwtFilter;
import com.mauamott.loginuser.security.jwt.SecurityContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
@Configuration
public class MainSecurity {

    private final SecurityContextRepository securityContextRepository;

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http, JwtFilter jwtFilter){
        return http
                .authorizeExchange()
                .pathMatchers("/auth/**").permitAll()
                .anyExchange().authenticated()
                .and()
                .addFilterAfter(jwtFilter, SecurityWebFiltersOrder.FIRST)
                .securityContextRepository(securityContextRepository)
                .formLogin().disable()
                .logout().disable()
                .httpBasic().disable()
                .csrf().disable()
                .build();
    }
}
