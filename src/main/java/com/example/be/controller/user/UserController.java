package com.example.be.controller.user;

import com.example.be.dto.user.UpdateMyProfileRequestDTO;
import com.example.be.dto.user.UserProfileDTO;
import com.example.be.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileDTO getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        return userService.getUserProfile(email);
    }

    @PutMapping("/me")
    public UserProfileDTO updateMyProfile(
            Authentication authentication,
            @RequestBody UpdateMyProfileRequestDTO request
    ) {
        String email = authentication.getName();
        return userService.updateMyProfile(email, request);
    }
}
