package com.app.apigateway.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver userKeyResolver() {

        return exchange -> {

            String user =
                    exchange
                            .getRequest()
                            .getHeaders()
                            .getFirst("X-User");

            if (user == null) {
                return Mono.just(
                        Objects.requireNonNull
                                        (exchange.getRequest()
                                        .getRemoteAddress())
                                .getAddress()
                                .getHostAddress()
                );
            }

            return Mono.just(user);
        };

    }
}
