package com.jobplatform.service;

import com.jobplatform.domain.Role;
import com.jobplatform.domain.User;
import com.jobplatform.dto.auth.*;
import com.jobplatform.repository.UserRepository;
import com.jobplatform.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(request.role() == null ? Role.CANDIDAT : request.role())
                .isEnabled(false)
                .isVerified(false)
                .build();
        // verification code (8 digits)
        String code = String.format("%08d", java.util.concurrent.ThreadLocalRandom.current().nextInt(0, 100_000_000));
        user.setVerificationToken(code);
        user.setTokenExpiry(LocalDateTime.now().plusDays(1));
        userRepository.save(user);

        emailService.send(
                user.getEmail(),
                "Vérification de compte",
                "Bonjour " + user.getFirstName() + ",\n\nVoici votre code de vérification : " + code +
                        "\nIl expire dans 24h. Rendez-vous sur la page Vérifier mon email et saisissez ce code.\n\nCordialement");
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.email(), request.password()));
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid credentials");
        }
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!Boolean.TRUE.equals(user.getIsEnabled())) {
            throw new IllegalStateException("Account not enabled");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        String accessToken = jwtService.generateToken(user.getEmail(), claims);
        // generate refresh token (UUID persisted)
        user.setRefreshToken(UUID.randomUUID().toString());
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepository.save(user);
        return AuthResponse.of(accessToken, user.getRefreshToken());
    }

    public void verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByVerificationToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (user.getTokenExpiry() != null && user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }
        user.setIsEnabled(true);
        user.setIsVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        User user = userRepository.findByRefreshToken(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (user.getRefreshTokenExpiry() == null || user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token expired");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        String accessToken = jwtService.generateToken(user.getEmail(), claims);
        return AuthResponse.of(accessToken, user.getRefreshToken());
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPasswordResetToken(UUID.randomUUID().toString());
        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(2));
        userRepository.save(user);
        String link = frontendUrl + "/reset-password?token=" + user.getPasswordResetToken();
        emailService.send(user.getEmail(), "Réinitialisation du mot de passe",
                "Bonjour,\n\nCliquez pour réinitialiser votre mot de passe : " + link);
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (user.getPasswordResetExpiry() == null || user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);
    }
}
