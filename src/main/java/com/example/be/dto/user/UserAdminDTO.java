package com.example.be.dto.user;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAdminDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String address;

    private String gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;

    private Boolean isVerified;
    private String status;
    private LocalDateTime createdAt;
    private Set<String> roles;
}
