package com.app.apigateway.filter;

import com.app.apigateway.config.GatewaySecurityProperties;
import com.app.apigateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.lettuce.core.ScriptOutputType;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class JwtAuthFilter implements GlobalFilter {

    @Value("${internal.secret}")
    private String gatewaySecret;

    private final JwtUtil jwtUtil;
    private final GatewaySecurityProperties props;

    public JwtAuthFilter(JwtUtil jwtUtil,
                         GatewaySecurityProperties props
    ) {
        this.jwtUtil = jwtUtil;
        this.props = props;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        // Always add gateway secret
        ServerWebExchange mutatedExchange =
                exchange.mutate()
                        .request(
                                exchange.getRequest()
                                        .mutate()
                                        .header("X-GATEWAY-KEY", gatewaySecret)
                                        .build()
                        )
                        .build();

        if (props.getAllowedPaths().contains(path)) {
            return chain.filter(mutatedExchange);
        }

        String authHeader = mutatedExchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return unauthorized(mutatedExchange);
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.validateToken(token);

            ServerWebExchange securedExchange =
                    mutatedExchange.mutate()
                            .request(
                                    mutatedExchange.getRequest()
                                            .mutate()
                                            .header("X-User", claims.getSubject())
                                            .header("X-Role",
                                                    claims.get("role", String.class))
                                            .build()
                            )
                            .build();

            return chain.filter(securedExchange);

        } catch (Exception e) {
            return unauthorized(mutatedExchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse()
                .setStatusCode(HttpStatus.UNAUTHORIZED);

        return exchange.getResponse().setComplete();
    }
}
