package com.app.notificationservice.client;

import com.app.notificationservice.dto.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceClient {

    private final AuthClient authClient;

    @CircuitBreaker(
            name = "authServiceCB",
            fallbackMethod = "fallbackUser"
    )
    @Retry(name = "authServiceRetry")
    public UserDto getUser(UUID userId) {
        return authClient.getUser(userId);
    }

    public UserDto fallbackUser(UUID userId, Throwable ex) {

        log.error("Auth service down for user {}", userId, ex);

        return UserDto.builder()
                .id(userId)
                .email("unknown@upi.com")
                .build();
    }
}