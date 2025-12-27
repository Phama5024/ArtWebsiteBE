package com.example.be.service.auth;

import com.example.be.dto.AuthResponseDTO;
import com.example.be.dto.LoginRequestDTO;
import com.example.be.dto.RegisterRequestDTO;
import com.example.be.entity.Role;
import com.example.be.entity.User;
import com.example.be.exception.EmailAlreadyExistsException;
import com.example.be.repository.role.RoleRepository;
import com.example.be.repository.user.UserRepository;
import com.example.be.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

        userRepository.save(user);
    }

    public AuthResponseDTO login(LoginRequestDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String role = user.getRoles().iterator().next().getName();
        String token = jwtService.generateToken(user.getEmail(), role);

        return new AuthResponseDTO(token, role);
    }
}
