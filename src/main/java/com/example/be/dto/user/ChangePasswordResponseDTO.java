package com.example.be.dto.user;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordResponseDTO {
    private String token;
    private String role;
    private UserProfileDTO profile;
}
