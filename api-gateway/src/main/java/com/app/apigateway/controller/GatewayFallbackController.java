package com.app.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class GatewayFallbackController {

    @GetMapping("/fallback/auth")
    public Mono<ResponseEntity<String>> authFallback() {
        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .body("Auth service temporarily unavailable"));
    }

    @GetMapping("/fallback/upi")
    public Mono<ResponseEntity<String>> upiFallback() {
        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .body("UPI service temporarily unavailable"));
    }

}