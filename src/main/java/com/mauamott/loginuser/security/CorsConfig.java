package com.mauamott.loginuser.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class CorsConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
                .allowedMethods("GET","POST","PUT","DELETE","PATH")
                .allowedOriginPatterns("*")
                .allowCredentials(true);
    }
}
