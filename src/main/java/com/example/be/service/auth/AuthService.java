package com.example.be.service.auth;

import com.example.be.dto.AuthResponseDTO;
import com.example.be.dto.LoginRequestDTO;
import com.example.be.dto.RegisterRequestDTO;
import com.example.be.entity.EmailVerificationToken;
import com.example.be.entity.PasswordResetToken;
import com.example.be.entity.Role;
import com.example.be.entity.User;
import com.example.be.exception.EmailAlreadyExistsException;
import com.example.be.repository.auth.EmailVerificationTokenRepository;
import com.example.be.repository.auth.PasswordResetTokenRepository;
import com.example.be.repository.role.RoleRepository;
import com.example.be.repository.user.UserRepository;
import com.example.be.security.jwt.JwtService;
import com.example.be.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Value("${app.frontend.reset-password-url}")
    private String frontendResetUrl;

    @Value("${app.auth.reset-token-exp-minutes:30}")
    private int resetTokenExpMinutes;

    @Transactional
    public void register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRoles(Set.of(userRole));
        user.setIsVerified(false);

        user.setTokenVersion(0);

        User saved = userRepository.save(user);

        createAndSendVerifyEmail(saved);
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String role = user.getRoles().iterator().next().getName();

        Integer tv = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
        String token = jwtService.generateToken(user.getEmail(), role, tv);

        return new AuthResponseDTO(token, role);
    }
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.invalidateAllActiveTokens(user.getId());

            PasswordResetToken prt = new PasswordResetToken();
            prt.setUser(user);
            prt.setToken(generateSecureToken());
            prt.setExpiresAt(LocalDateTime.now().plusMinutes(resetTokenExpMinutes));
            prt.setUsed(false);

            passwordResetTokenRepository.save(prt);

            String link = frontendResetUrl + "?token=" + prt.getToken();
            mailService.sendResetPasswordEmail(user.getEmail(), link);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (prt.isUsed()) throw new RuntimeException("Token already used");
        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) throw new RuntimeException("Token expired");

        User user = prt.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));

        Integer cur = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
        user.setTokenVersion(cur + 1);

        userRepository.save(user);

        prt.setUsed(true);
        prt.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(prt);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Value("${app.frontend.verify-email-url}")
    private String frontendVerifyUrl;

    @Value("${app.auth.verify-token-exp-minutes:60}")
    private int verifyTokenExpMinutes;

    @Transactional
    public void createAndSendVerifyEmail(User user) {
        emailVerificationTokenRepository.invalidateAllActiveTokens(user.getId());

        EmailVerificationToken evt = new EmailVerificationToken();
        evt.setUser(user);
        evt.setToken(generateSecureToken());
        evt.setExpiresAt(LocalDateTime.now().plusMinutes(verifyTokenExpMinutes));
        evt.setUsed(false);

        emailVerificationTokenRepository.save(evt);

        String link = frontendVerifyUrl + "?token=" + evt.getToken();
        mailService.sendVerifyEmail(user.getEmail(), link);
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken evt = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (evt.isUsed()) throw new RuntimeException("Token already used");
        if (evt.getExpiresAt().isBefore(LocalDateTime.now())) throw new RuntimeException("Token expired");

        User user = evt.getUser();
        user.setIsVerified(true);
        userRepository.save(user);

        evt.setUsed(true);
        evt.setUsedAt(LocalDateTime.now());
        emailVerificationTokenRepository.save(evt);
    }
}
