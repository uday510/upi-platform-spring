package com.app.authservice.service;

import com.app.authservice.dto.LoginRequestDTO;
import com.app.authservice.dto.LoginResponseDTO;
import com.app.authservice.dto.RegisterRequestDTO;
import com.app.authservice.entity.User;
import com.app.authservice.exception.AuthException; // Custom Exception
import com.app.authservice.repository.UserRepository;
import com.app.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
        log.info("Attempting to register user: {}", requestDTO.getEmail());

        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            log.warn("Registration failed: Email {} already exists", requestDTO.getEmail());
            throw new AuthException("Email is already registered", HttpStatus.CONFLICT);
        }

        User newUser = User.builder()
                .email(requestDTO.getEmail())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .role("USER")
                .build();

        userRepository.save(newUser);
        log.info("User registered successfullÏy: {}", requestDTO.getEmail());
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
        log.info("Login attempt for user: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User {} not found", dto.getEmail());
                    return new AuthException("Invalid email or password", HttpStatus.UNAUTHORIZED);
                });

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Login failed: Incorrect password for user {}", dto.getEmail());
            throw new AuthException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtService.generateJWT(
                user.getId(),
                user.getRole()
        );

        return new LoginResponseDTO(token);
    }
}