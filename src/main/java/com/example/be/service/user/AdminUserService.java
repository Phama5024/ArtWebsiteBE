package com.example.be.service.user;


import com.example.be.dto.user.UpdateUserAdminRequestDTO;
import com.example.be.dto.user.UserAdminDTO;
import com.example.be.entity.User;
import com.example.be.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    private UserAdminDTO toDto(User u) {
        return new UserAdminDTO(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getPhone(),
                u.getAddress(),

                u.getGender(),
                u.getDateOfBirth(),
                u.getAvatarUrl(),

                u.getIsVerified(),
                u.getStatus(),
                u.getCreatedAt(),
                u.getRoles() == null ? null :
                        u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet())
        );
    }


    public Page<UserAdminDTO> listUsers(String q, String status, Pageable pageable) {
        return userRepository.adminFindUsers(q, status, pageable).map(this::toDto);
    }

    public UserAdminDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.getRoles().size();
        return toDto(user);
    }

    @Transactional
    public UserAdminDTO updateUser(Long id, UpdateUserAdminRequestDTO req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getAddress() != null) user.setAddress(req.getAddress());
        if (req.getIsVerified() != null) user.setIsVerified(req.getIsVerified());
        if (req.getStatus() != null) user.setStatus(req.getStatus());
        if (req.getGender() != null) user.setGender(req.getGender());
        if (req.getDateOfBirth() != null) user.setDateOfBirth(req.getDateOfBirth());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());


        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserAdminDTO lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus("LOCKED");
        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserAdminDTO unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus("ACTIVE");
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRoles() != null) user.getRoles().clear();

        userRepository.delete(user);
    }
}
