package com.app.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();

        String traceId = UUID.randomUUID().toString();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(exchange.getRequest()
                        .mutate()
                        .header("X-Trace-Id", traceId)
                        .build())
                .build();

        return Mono.defer(() -> {

            MDC.put("traceId", traceId);

            ServerHttpRequest request = mutatedExchange.getRequest();

            String path = request.getURI().getPath();
            String method = request.getMethod().name();
            String ip = request.getRemoteAddress() != null
                    ? request.getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";

            log.info("Incoming request | method={} path={} ip={}",
                    method, path, ip);

            return chain.filter(mutatedExchange)
                    .doOnSuccess(aVoid -> {
                        long duration = System.currentTimeMillis() - startTime;

                        int status = mutatedExchange.getResponse()
                                .getStatusCode() != null
                                ? mutatedExchange.getResponse()
                                .getStatusCode().value()
                                : 0;

                        log.info("Response | method={} path={} status={} duration={}ms",
                                method, path, status, duration);
                    })
                    .doFinally(signal -> {
                        MDC.clear();
                    });

        });
    }

    @Override
    public int getOrder() {
        return -200;
    }
}