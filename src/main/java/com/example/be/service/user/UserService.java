package com.example.be.service.user;

import com.example.be.dto.user.ChangeMyPasswordRequestDTO;
import com.example.be.dto.user.ChangePasswordResponseDTO;
import com.example.be.dto.user.UpdateMyProfileRequestDTO;
import com.example.be.dto.user.UserProfileDTO;
import com.example.be.entity.User;
import com.example.be.repository.user.UserRepository;
import com.example.be.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserProfileDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toProfileDTO(user);
    }

    @Transactional
    public UserProfileDTO updateMyProfile(String email, UpdateMyProfileRequestDTO req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        user.setGender(req.getGender());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setAddress(req.getAddress());

        userRepository.save(user);
        return toProfileDTO(user);
    }

    @Transactional
    public ChangePasswordResponseDTO changeMyPassword(String email, ChangeMyPasswordRequestDTO req) {

        if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Current password is required");
        }
        if (req.getNewPassword() == null || req.getNewPassword().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "New password is required");
        }
        if (req.getConfirmNewPassword() == null || req.getConfirmNewPassword().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Confirm password is required");
        }
        if (!req.getNewPassword().equals(req.getConfirmNewPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "Confirm password does not match");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "User not found"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "Current password is incorrect");
        }

        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "New password must be different");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));

        Integer cur = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
        user.setTokenVersion(cur + 1);

        userRepository.save(user);

        String role = user.getRoles().iterator().next().getName();
        String token = jwtService.generateToken(user.getEmail(), role, user.getTokenVersion());

        return new ChangePasswordResponseDTO(token, role, toProfileDTO(user));
    }

    private UserProfileDTO toProfileDTO(User user) {
        return new UserProfileDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getGender(),
                user.getDateOfBirth(),
                user.getAvatarUrl(),
                user.getAddress(),
                user.getIsVerified(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet())
        );
    }

    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
