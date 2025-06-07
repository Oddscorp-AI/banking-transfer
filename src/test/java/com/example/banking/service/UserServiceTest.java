package com.example.banking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.banking.dto.RegistrationRequest;
import com.example.banking.model.User;
import com.example.banking.model.UserRole;
import com.example.banking.repository.UserRepository;

class UserServiceTest {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(any())).thenAnswer(i -> "ENC-" + i.getArgument(0));
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registerNewUser() {
        RegistrationRequest req = new RegistrationRequest("a@b.com", "pass", "111", "t", "e", "123456");
        User saved = new User();
        saved.setEmail("a@b.com");
        when(userRepository.save(any())).thenReturn(saved);

        User result = userService.register(req);
        assertEquals("a@b.com", result.getEmail());
        verify(userRepository).save(any());
    }

    @Test
    void duplicateEmailThrows() {
        RegistrationRequest req = new RegistrationRequest("a@b.com", "pass", "111", "t", "e", "123456");
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    @Test
    void duplicateCitizenThrows() {
        RegistrationRequest req = new RegistrationRequest("a@b.com", "pass", "111", "t", "e", "123456");
        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
        when(userRepository.existsByCitizenId("111")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }
}
