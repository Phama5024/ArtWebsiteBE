package com.example.be.dto.user;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMyProfileRequestDTO {
    private String fullName;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
}
