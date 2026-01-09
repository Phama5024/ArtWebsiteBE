package com.example.be.service.user;

import com.example.be.dto.user.UploadAvatarResponseDTO;
import com.example.be.entity.User;
import com.example.be.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private final UserRepository userRepository;

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private String getExt(MultipartFile file) {
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int dot = original.lastIndexOf(".");
        String ext = dot >= 0 ? original.substring(dot).toLowerCase() : "";
        if (!ext.isBlank()) return ext;

        return switch (String.valueOf(file.getContentType())) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private void deleteOldAvatarIfExists(User user) {
        try {
            String old = user.getAvatarUrl();
            if (old == null || old.isBlank()) return;
            if (!old.startsWith("/uploads/")) return;

            String oldFileName = old.substring("/uploads/".length());
            Path oldPath = UPLOAD_DIR.resolve(oldFileName);
            Files.deleteIfExists(oldPath);
        } catch (Exception ignored) {}
    }

    private UploadAvatarResponseDTO saveAvatarForUser(User user, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new RuntimeException("File is empty");
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new RuntimeException("Only JPG/PNG/WEBP allowed");

        Files.createDirectories(UPLOAD_DIR);

        deleteOldAvatarIfExists(user);

        String ext = getExt(file);
        String filename = "avatar_" + user.getId() + "_" + UUID.randomUUID() + ext;

        Path target = UPLOAD_DIR.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String publicUrl = "/uploads/" + filename;
        user.setAvatarUrl(publicUrl);
        userRepository.save(user);

        return new UploadAvatarResponseDTO(publicUrl);
    }

    @Transactional
    public UploadAvatarResponseDTO uploadMyAvatar(String email, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return saveAvatarForUser(user, file);
    }

    @Transactional
    public UploadAvatarResponseDTO adminUploadAvatar(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return saveAvatarForUser(user, file);
    }
}
