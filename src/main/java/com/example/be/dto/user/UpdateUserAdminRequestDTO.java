package com.example.be.dto.user;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserAdminRequestDTO {
    private String fullName;
    private String phone;
    private String address;

    private String gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;

    private Boolean isVerified;
    private String status;
}