package com.example.be.service.product;

import com.example.be.dto.product.*;
import com.example.be.entity.Category;
import com.example.be.entity.Product;
import com.example.be.entity.ProductMeta;
import com.example.be.repository.category.CategoryRepository;
import com.example.be.repository.product.ProductRepository;
import com.example.be.service.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewService reviewService;

    @Override
    public ProductResponseDTO create(ProductRequestDTO request) {

        Set<Category> categories =
                categoryRepository.findAllById(request.getCategoryIds())
                        .stream()
                        .collect(Collectors.toSet());

        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setStatus(request.getStatus());
        product.setCategories(categories);

        ProductMeta meta = ProductMeta.builder()
                .fileFormat(request.getFileFormat())
                .resolution(request.getResolution())
                .aspectRatio(request.getAspectRatio())
                .fileSize(request.getFileSize())
                .author(request.getAuthor())
                .style(request.getStyle())
                .origin(request.getOrigin())
                .characterName(request.getCharacterName())
                .extraInfo(request.getExtraInfo())

                .product(product)
                .build();

        product.setMeta(meta);

        Product savedProduct = productRepository.save(product);

        return mapToDTO(savedProduct);
    }


    @Override
    public ProductResponseDTO update(Long id, ProductRequestDTO request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if ("LOCKED".equals(product.getStatus())) {
            throw new RuntimeException("Sản phẩm đang bị khóa, không thể chỉnh sửa");
        }

        Set<Category> categories = categoryRepository
                .findAllById(request.getCategoryIds())
                .stream()
                .collect(Collectors.toSet());

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setStatus(request.getStatus());
        product.setCategories(categories);

        ProductMeta meta = product.getMeta();
        if (meta == null) {
            meta = new ProductMeta();
            meta.setProduct(product);
        }

        meta.setFileFormat(request.getFileFormat());
        meta.setResolution(request.getResolution());
        meta.setAspectRatio(request.getAspectRatio());
        meta.setFileSize(request.getFileSize());
        meta.setAuthor(request.getAuthor());
        meta.setStyle(request.getStyle());
        meta.setOrigin(request.getOrigin());
        meta.setCharacterName(request.getCharacterName());
        meta.setExtraInfo(request.getExtraInfo());

        product.setMeta(meta);

        return mapToDTO(productRepository.save(product));
    }

    @Override
    public List<ProductResponseDTO> getAll() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDTO getById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    private ProductResponseDTO mapToDTO(Product product) {

        String primaryImageUrl = null;
        var summary = reviewService.getRatingSummary(product.getId());

        double avg = summary == null ? 0.0 : (summary.getAverageRating() == null ? 0.0 : summary.getAverageRating());
        long count = summary == null ? 0L : (long) summary.getTotalReviews();

        if (product.getMediaList() != null && !product.getMediaList().isEmpty()) {
            primaryImageUrl = product.getMediaList().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsPrimary()))
                    .map(m -> m.getImageUrl())
                    .findFirst()
                    .orElse(product.getMediaList().get(0).getImageUrl());
        }

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .categories(
                        product.getCategories()
                                .stream()
                                .map(Category::getName)
                                .collect(Collectors.toSet())
                )
                .primaryImageUrl(primaryImageUrl)

                // ✅ add
                .ratingAvg(avg)
                .ratingCount(count)

                .build();

    }


    @Override
    public ProductDetailResponseDTO getProductDetail(Long id) {

        Product product = productRepository.findDetailById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return ProductDetailResponseDTO.builder()

                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())

                .categories(
                        product.getCategories()
                                .stream()
                                .map(Category::getName)
                                .collect(Collectors.toSet())
                )

                .images(
                        product.getMediaList()
                                .stream()
                                .map(media -> ProductMediaDTO.builder()
                                        .id(media.getId())
                                        .imageUrl(media.getImageUrl())
                                        .isPrimary(media.getIsPrimary())
                                        .build()
                                )
                                .toList()
                )

                .meta(
                        product.getMeta() == null ? null :
                                ProductMetaDTO.builder()
                                        .fileFormat(product.getMeta().getFileFormat())
                                        .resolution(product.getMeta().getResolution())
                                        .aspectRatio(product.getMeta().getAspectRatio())
                                        .fileSize(product.getMeta().getFileSize())
                                        .author(product.getMeta().getAuthor())
                                        .style(product.getMeta().getStyle())
                                        .origin(product.getMeta().getOrigin())
                                        .characterName(product.getMeta().getCharacterName())
                                        .extraInfo(product.getMeta().getExtraInfo())
                                        .build()
                )

                .rating(reviewService.getRatingSummary(id))
                .reviews(reviewService.getReviewsByProduct(id))

                .build();
    }

    @Override
    public Page<ProductResponseDTO> getAllPaged(
            int page,
            int size,
            String sort,
            String category
    ) {
        Sort sortBy = switch (sort) {
            case "newest" -> Sort.by("createdAt").descending();
            case "oldest" -> Sort.by("createdAt").ascending();
            case "a-z" -> Sort.by("name").ascending();
            case "z-a" -> Sort.by("name").descending();
            default -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sortBy);
        Page<Product> pageResult =
                (category == null || category.isBlank())
                        ? productRepository.findAllActiveWithMedia(pageable)
                        : productRepository.findByProductCategoryWithMedia(category, pageable);

        return pageResult.map(this::mapToDTO);
    }

    @Override
    @Transactional
    public void softDeleteMany(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("Danh sách sản phẩm trống");
        }
        productRepository.softDeleteMany(ids);
    }

    @Override
    @Transactional
    public void lockProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("Danh sách sản phẩm trống");
        }
        productRepository.lockMany(ids);
    }

    @Override
    @Transactional
    public void unlockProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("Danh sách sản phẩm trống");
        }
        productRepository.unlockMany(ids);
    }


}
