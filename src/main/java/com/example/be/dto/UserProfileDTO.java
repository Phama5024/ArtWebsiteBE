package com.example.be.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private Boolean isVerified;
    private String status;
    private LocalDateTime createdAt;
    private Set<String> roles;
}
