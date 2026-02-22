package com.app.authservice.controller;

import com.app.authservice.dto.UserDto;
import com.app.authservice.entity.User;
import com.app.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}