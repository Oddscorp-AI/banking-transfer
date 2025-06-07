package com.example.banking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.TokenResponse;
import com.example.banking.model.UserRole;
import com.example.banking.repository.UserRepository;
import com.example.banking.security.JwtService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> customerLogin(@RequestBody LoginRequest request) {
        var user = userRepository.findByEmail(request.email())
                .filter(u -> u.getRole() == UserRole.CUSTOMER)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/teller/login")
    public ResponseEntity<TokenResponse> tellerLogin(@RequestBody LoginRequest request) {
        var user = userRepository.findByEmail(request.email())
                .filter(u -> u.getRole() == UserRole.TELLER)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(new TokenResponse(token));
    }
}
