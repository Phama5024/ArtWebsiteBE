package com.example.be.service.product;

import com.example.be.dto.product.ProductMediaDTO;
import com.example.be.entity.Product;
import com.example.be.entity.ProductMedia;
import com.example.be.repository.product.ProductMediaRepository;
import com.example.be.repository.product.ProductRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductMediaServiceImpl implements ProductMediaService {

    private final ProductRepository productRepository;
    private final ProductMediaRepository mediaRepository;

    private static final String UPLOAD_DIR = "uploads/products/";

    @Override
    public ProductMediaDTO uploadProductImage(Long productId, MultipartFile file, boolean isPrimary) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, file.getBytes());

            ProductMedia media = ProductMedia.builder()
                    .imageUrl("/uploads/products/" + fileName)
                    .isPrimary(isPrimary)
                    .product(product)
                    .build();

            ProductMedia saved = mediaRepository.save(media);

            return ProductMediaDTO.builder()
                    .id(saved.getId())
                    .imageUrl(saved.getImageUrl())
                    .isPrimary(saved.getIsPrimary())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }
}
