package com.app.authservice.service;

import com.app.authservice.dto.LoginRequestDTO;
import com.app.authservice.dto.LoginResponseDTO;
import com.app.authservice.dto.RegisterRequestDTO;
import com.app.authservice.model.User;
import com.app.authservice.repository.UserRepository;
import com.app.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register(RegisterRequestDTO requestDTO) {


        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User newUser = User.builder()
                .email(requestDTO.getEmail())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .role("USER")
                .build();

        userRepository.save(newUser);

    }

    public LoginResponseDTO login(LoginRequestDTO dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        String token = jwtService.generateJWT(
                user.getEmail(),
                user.getRole()
        );

        return new LoginResponseDTO(token);
    }

}
