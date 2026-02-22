package com.app.notificationservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthFeignConfig {

    @Value("${internal.secret}")
    private String internalSecret;

    @Bean
    public RequestInterceptor internalAuthInterceptor() {

        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header("X-INTERNAL-KEY", internalSecret);
            }
        };
    }
}