package com.peap.identity.service;

import com.peap.identity.dto.LoginRequest;
import com.peap.identity.dto.RegisterRequest;
import com.peap.identity.event.UserEventPublisher;
import com.peap.identity.model.User;
import com.peap.identity.model.UserRole;
import com.peap.identity.repository.UserRepository;
import com.peap.identity.security.JwtService;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserEventPublisher eventPublisher;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("an account with this email already exists");
        }
        User user = new User(
                UUID.randomUUID(),
                request.email(),
                passwordEncoder.encode(request.password()),
                UserRole.REGISTERED_USER,
                Instant.now());
        userRepository.save(user);
        eventPublisher.publishUserCreated(user);
        return user;
    }

    public String authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NoSuchElementException("invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new NoSuchElementException("invalid credentials");
        }
        return jwtService.issueToken(user.getId(), user.getEmail(), user.getRole().name());
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("user not found: " + id));
    }
}
