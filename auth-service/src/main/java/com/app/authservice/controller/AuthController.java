package com.app.authservice.controller;

import com.app.authservice.dto.LoginRequestDTO;
import com.app.authservice.dto.LoginResponseDTO;
import com.app.authservice.dto.RegisterRequestDTO;
import com.app.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequestDTO requestDTO
            ) {

        authService.register(requestDTO);

        return ResponseEntity.ok(HttpStatus.CREATED.getReasonPhrase());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO requestDTO
            ) {

        return ResponseEntity.ok(authService.login(requestDTO));
    }

}
