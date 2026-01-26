package com.example.be.service.user;

import com.example.be.dto.user.UpdateMyProfileRequestDTO;
import com.example.be.dto.user.UserProfileDTO;
import com.example.be.entity.User;
import com.example.be.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
