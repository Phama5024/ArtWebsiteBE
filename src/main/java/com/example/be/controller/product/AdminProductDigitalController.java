package com.example.be.controller.product;

import com.example.be.entity.Product;
import com.example.be.entity.ProductMeta;
import com.example.be.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SELLER')")
public class AdminProductDigitalController {

    private final ProductRepository productRepository;

    @PostMapping("/{id}/digital-file")
    @Transactional
    public ProductMeta uploadDigital(@PathVariable Long id,
                                     @RequestParam("file") MultipartFile file) throws Exception {

        Product p = productRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        if (p.getMeta() == null) {
            ProductMeta meta = new ProductMeta();
            meta.setProduct(p);
            p.setMeta(meta);
        }

        Path base = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(base);

        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String safeName = UUID.randomUUID() + "_" + original.replaceAll("[\\\\/]", "_");

        Path target = base.resolve("products").resolve(safeName).normalize();
        Files.createDirectories(target.getParent());

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String relPath = base.relativize(target).toString().replace("\\", "/");

        p.getMeta().setDownloadPath(relPath);
        p.getMeta().setDownloadName(original);
        p.getMeta().setDownloadMime(file.getContentType());
        p.getMeta().setDownloadSize(file.getSize());

        productRepository.save(p);
        return p.getMeta();
    }
}
