package com.taskmanagement.taskmanagement.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.taskmanagement.taskmanagement.dto.AuthRequest;
import com.taskmanagement.taskmanagement.dto.AuthResponse;
import com.taskmanagement.taskmanagement.dto.RegisterRequest;
import com.taskmanagement.taskmanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication",description = "Endpoints for user registration and login")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with USER role. " +
                      "Returns a JWT token immediately after registration. " +
                      "Email must be unique."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        // Tells Swagger: 201 response contains AuthResponse object

        @ApiResponse(
            responseCode = "400",
            description = "Email already registered or validation failed"
        ),

        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/login")
     @Operation(
        summary = "Login with email and password",
        description = "Authenticates user credentials and returns a JWT token. " +
                      "Use this token in the Authorization header as: Bearer {token}"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful, JWT token returned",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid email or password"
        )
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
