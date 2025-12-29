package com.example.be.controller.product;

import com.example.be.dto.product.ProductMediaDTO;
import com.example.be.service.product.ProductMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProductMediaController {

    private final ProductMediaService productMediaService;

    @PostMapping("/{id}/images")
    public ProductMediaDTO uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean isPrimary
    ) {
        return productMediaService.uploadProductImage(id, file, isPrimary);
    }
}
