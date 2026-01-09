package com.example.be.controller.user;

import com.example.be.dto.user.UploadAvatarResponseDTO;
import com.example.be.service.user.UserAvatarService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAvatarController {

    private final UserAvatarService userAvatarService;

    @PostMapping("/me/avatar")
    public UploadAvatarResponseDTO uploadMyAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        String email = authentication.getName();
        return userAvatarService.uploadMyAvatar(email, file);
    }
}
