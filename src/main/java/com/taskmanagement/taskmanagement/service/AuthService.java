package com.taskmanagement.taskmanagement.service;

import com.taskmanagement.taskmanagement.dto.AuthRequest;
import com.taskmanagement.taskmanagement.dto.AuthResponse;
import com.taskmanagement.taskmanagement.dto.RegisterRequest;
import com.taskmanagement.taskmanagement.exception.BadRequestException;
import com.taskmanagement.taskmanagement.model.User;
import com.taskmanagement.taskmanagement.repository.UserRepository;
import com.taskmanagement.taskmanagement.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                "Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(token,user.getRole().name(),user.getName(),"Registration successful! Welcome " + user.getName());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new BadRequestException(
                "No account found with email: "
                + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect password!");
        }

        String token = jwtUtil.generateToken(
            user.getEmail(), user.getRole().name());

        return new AuthResponse(
            token,
            user.getRole().name(),
            user.getName(),
            "Welcome back, " + user.getName() + "!");
    }
}
