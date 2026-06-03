package com.taskmanagement.taskmanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskmanagement.taskmanagement.dto.AuthRequest;
import com.taskmanagement.taskmanagement.dto.AuthResponse;
import com.taskmanagement.taskmanagement.dto.RegisterRequest;
import com.taskmanagement.taskmanagement.exception.BadRequestException;
import com.taskmanagement.taskmanagement.model.User;
import com.taskmanagement.taskmanagement.repository.UserRepository;
import com.taskmanagement.taskmanagement.security.JwtUtil;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    // Fake UserRepository — no real database!

    @Mock
    private JwtUtil jwtUtil;
    // Fake JwtUtil — returns fake tokens
    // We don't test JWT logic here — only AuthService logic!

    @Mock
    private PasswordEncoder passwordEncoder;
    // Fake PasswordEncoder — returns predictable values
    // We don't test BCrypt here — only AuthService logic!

    @InjectMocks
    private AuthService authService;
    // REAL AuthService with all fakes injected

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        // Fresh test data before each test

        registerRequest = new RegisterRequest();
        registerRequest.setName("Sumit Uppal");
        registerRequest.setEmail("sumit@gmail.com");
        registerRequest.setPassword("password123");

        authRequest = new AuthRequest();
        authRequest.setEmail("sumit@gmail.com");
        authRequest.setPassword("password123");

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Sumit Uppal");
        existingUser.setEmail("sumit@gmail.com");
        existingUser.setPassword("$2a$10$hashedpassword");
        // Stored password is BCrypt hash
        // NEVER plain text!
        existingUser.setRole(User.Role.USER);
    }

    // ═══════════════════════════════════════════════════════════
    // REGISTER TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Register successfully with new email")
    void register_WithNewEmail_ReturnsAuthResponse() {

        // ── ARRANGE ──────────────────────────────────────────
        // Email does NOT exist yet → false
        when(userRepository.existsByEmail("sumit@gmail.com"))
                .thenReturn(false);

        // Fake BCrypt: encode returns a fake hash
        when(passwordEncoder.encode("password123"))
                .thenReturn("$2a$10$hashedpassword");
        // Real BCrypt would return a different hash each time
        // Mocking it gives us a predictable value!

        // Fake save: return the user object
        when(userRepository.save(any(User.class)))
                .thenReturn(existingUser);

        // Fake JWT: return a fake token string
        when(jwtUtil.generateToken(
                anyString(), anyString()))
                .thenReturn("fake.jwt.token");
        // anyString() = we don't care what exact strings are passed

        // ── ACT ───────────────────────────────────────────────
        AuthResponse response = authService
                .register(registerRequest);

        // ── ASSERT ────────────────────────────────────────────
        assertNotNull(response);
        assertNotNull(response.getToken());
        // Token must exist in response

        assertEquals("USER", response.getRole());
        // New user must have USER role

        assertEquals("Sumit Uppal", response.getName());
        // Name in response must match

        // Verify password was encoded before saving
        // This is critical security test!
        verify(passwordEncoder, times(1))
                .encode("password123");

        // Verify user was saved to database
        verify(userRepository, times(1))
                .save(any(User.class));

        // Verify JWT was generated
        verify(jwtUtil, times(1))
                .generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Register fails when email already exists")
    void register_WithExistingEmail_ThrowsBadRequestException() {

        // ── ARRANGE ──────────────────────────────────────────
        // Email ALREADY EXISTS → true
        when(userRepository.existsByEmail("sumit@gmail.com"))
                .thenReturn(true);

        // ── ACT + ASSERT ──────────────────────────────────────
        BadRequestException exception = assertThrows(
                BadRequestException.class, () ->
                authService.register(registerRequest));
        // assertThrows returns the exception object
        // so you can check its message too!

        assertTrue(exception.getMessage()
                .contains("already registered"));
        // Message must contain "already registered"

        // Most important: user must NEVER be saved!
        verify(userRepository, never())
                .save(any(User.class));

        // Password must NEVER be encoded (no point if email exists)
        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    @DisplayName("Register encodes password before saving (security test)")
    void register_PasswordIsEncoded_BeforeSaving() {

        // This test specifically verifies security behaviour
        // Plain text password must NEVER be saved!

        // ── ARRANGE ──────────────────────────────────────────
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$10$encodedpassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(existingUser);
        when(jwtUtil.generateToken(anyString(), anyString()))
                .thenReturn("fake.jwt.token");

        // ── ACT ───────────────────────────────────────────────
        authService.register(registerRequest);

        // ── ASSERT ────────────────────────────────────────────
        // Capture what was saved to verify password was encoded
        verify(passwordEncoder).encode("password123");
        // If this fails → plain text password being saved!
        // That would be a critical security bug!
    }

    // ═══════════════════════════════════════════════════════════
    // LOGIN TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Login successfully with correct credentials")
    void login_WithValidCredentials_ReturnsAuthResponse() {

        // ── ARRANGE ──────────────────────────────────────────
        when(userRepository.findByEmail("sumit@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        // Fake BCrypt matches: return true (password correct)
        when(passwordEncoder.matches(
                "password123",
                "$2a$10$hashedpassword"))
                .thenReturn(true);
        // First param  = what user typed (plain text)
        // Second param = what's stored in DB (hash)
        // Returns true = they match!

        when(jwtUtil.generateToken(anyString(), anyString()))
                .thenReturn("valid.jwt.token");

        // ── ACT ───────────────────────────────────────────────
        AuthResponse response = authService.login(authRequest);

        // ── ASSERT ────────────────────────────────────────────
        assertNotNull(response);
        assertEquals("valid.jwt.token", response.getToken());
        assertEquals("USER", response.getRole());
        assertNotNull(response.getName());

        // Verify JWT was generated after successful login
        verify(jwtUtil, times(1))
                .generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Login fails when email not registered")
    void login_WithUnregisteredEmail_ThrowsBadRequestException() {

        // ── ARRANGE ──────────────────────────────────────────
        // User not found in database
        when(userRepository.findByEmail("notexist@gmail.com"))
                .thenReturn(Optional.empty());

        authRequest.setEmail("notexist@gmail.com");

        // ── ACT + ASSERT ──────────────────────────────────────
        assertThrows(BadRequestException.class, () ->
                authService.login(authRequest));

        // Password check must NEVER happen (no user found)
        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Login fails when password is incorrect")
    void login_WithWrongPassword_ThrowsBadRequestException() {

        // ── ARRANGE ──────────────────────────────────────────
        when(userRepository.findByEmail("sumit@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        // Fake BCrypt: return false (password WRONG)
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        // ── ACT + ASSERT ──────────────────────────────────────
        BadRequestException exception = assertThrows(
                BadRequestException.class, () ->
                authService.login(authRequest));

        assertTrue(exception.getMessage()
                .toLowerCase().contains("password"));
        // Error message must mention password

        // JWT must NEVER be generated for wrong password
        verify(jwtUtil, never())
                .generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Login response contains correct user info")
    void login_ReturnsCorrectUserDetails() {

        // ── ARRANGE ──────────────────────────────────────────
        when(userRepository.findByEmail("sumit@gmail.com"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);
        when(jwtUtil.generateToken("sumit@gmail.com", "USER"))
                .thenReturn("generated.token.here");

        // ── ACT ───────────────────────────────────────────────
        AuthResponse response = authService.login(authRequest);

        // ── ASSERT ────────────────────────────────────────────
        assertEquals("Sumit Uppal", response.getName());
        assertEquals("USER", response.getRole());
        assertEquals("generated.token.here", response.getToken());
        assertTrue(response.getMessage()
                .contains("Sumit Uppal"));
        // Welcome message should contain user's name
    }
}
