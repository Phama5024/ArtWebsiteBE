package com.example.be.service.category;

import com.example.be.dto.category.CategoryResponseDTO;
import com.example.be.entity.Product;
import com.example.be.entity.ProductMedia;
import com.example.be.repository.category.CategoryRepository;
import com.example.be.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public List<CategoryResponseDTO> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(cat -> CategoryResponseDTO.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .build())
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDTO> getAllWithThumbnail() {

        Sort sortByNewest = Sort.by("createdAt").descending();
        PageRequest top1 = PageRequest.of(0, 1, sortByNewest);

        return categoryRepository.findAll()
                .stream()
                .map(cat -> {
                    String thumb = findThumbnailByCategory(cat.getName(), top1);

                    return CategoryResponseDTO.builder()
                            .id(cat.getId())
                            .name(cat.getName())
                            .thumbnailUrl(thumb)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String findThumbnailByCategory(String categoryName, PageRequest top1) {
        List<Product> list = productRepository.findTopActiveByCategoryWithMedia(categoryName, top1);
        if (list == null || list.isEmpty()) return null;

        Product p = list.get(0);
        if (p.getMediaList() == null || p.getMediaList().isEmpty()) return null;

        return p.getMediaList().stream()
                .sorted(Comparator.comparing(m -> Boolean.TRUE.equals(m.getIsPrimary()) ? 0 : 1))
                .map(ProductMedia::getImageUrl)
                .findFirst()
                .orElse(null);
    }
}
