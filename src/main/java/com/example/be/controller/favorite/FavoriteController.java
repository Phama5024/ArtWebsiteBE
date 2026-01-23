package com.example.be.controller.favorite;

import com.example.be.repository.user.UserRepository;
import com.example.be.service.favorite.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    @PostMapping("/{productId}/favorite")
    public ResponseEntity<Void> like(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        favoriteService.like(userId, productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}/favorite")
    public ResponseEntity<Void> unlike(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        favoriteService.unlike(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favorites/me")
    public ResponseEntity<?> myFavorites(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(favoriteService.getMyFavorites(userId));
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Unauthenticated");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
    }
}
