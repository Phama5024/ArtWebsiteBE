package com.example.be.dto.user;

import lombok.Data;

@Data
public class ChangeMyPasswordRequestDTO {
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;
}
