package com.example.be.service.storage;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@Component
public class CommissionFileStorage {

    private final Path root = Paths.get("uploads/commissions");

    public String save(Long requestId, MultipartFile file) throws Exception {
        Path dir = root.resolve(String.valueOf(requestId));
        Files.createDirectories(dir);

        String original = file.getOriginalFilename();
        String ext = "";
        if (StringUtils.hasText(original) && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }

        String filename = UUID.randomUUID() + ext;
        Path target = dir.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/commissions/" + requestId + "/" + filename;
    }

    public void deleteByPublicPath(String publicPath) {
        if (!StringUtils.hasText(publicPath)) return;

        String normalized = publicPath.startsWith("/") ? publicPath.substring(1) : publicPath;
        Path file = Paths.get(normalized).normalize();

        if (!file.startsWith(Paths.get("uploads"))) return;

        try {
            Files.deleteIfExists(file);
        } catch (Exception ignored) {
        }
    }
}
