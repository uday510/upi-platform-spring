package com.app.notificationservice.client;

import com.app.notificationservice.config.AuthFeignConfig;
import com.app.notificationservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "auth-service",
        configuration = AuthFeignConfig.class
)
public interface AuthClient {

    @GetMapping("/internal/users/{id}")
    UserDto getUser(@PathVariable UUID id);
}