package com.app.authservice.service;

import com.app.authservice.dto.LoginRequestDTO;
import com.app.authservice.dto.LoginResponseDTO;
import com.app.authservice.dto.RegisterRequestDTO;
import com.app.authservice.entity.User;
import com.app.authservice.exception.EmailAlreadyExistsException;
import com.app.authservice.exception.InvalidCredentialsException;
import com.app.authservice.repository.UserRepository;
import com.app.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public void register(RegisterRequestDTO requestDTO) {

        log.info("Register attempt for email: {}", requestDTO.getEmail());

        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            log.warn("Registration failed - email already exists: {}", requestDTO.getEmail());
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User newUser = User.builder()
                .email(requestDTO.getEmail())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .role("USER")
                .build();

        userRepository.save(newUser);

        log.info("User registered successfully: {}", requestDTO.getEmail());
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {

        log.info("Login attempt for email: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", dto.getEmail());
                    return new InvalidCredentialsException("User not registered.");
                });

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Login failed - wrong password for: {}", dto.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateJWT(
                user.getId(),
                user.getRole()
        );

        log.info("Login successful for email: {}", dto.getEmail());

        return new LoginResponseDTO(token);
    }
}