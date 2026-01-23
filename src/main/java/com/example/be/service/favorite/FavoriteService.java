package com.example.be.service.favorite;


import com.example.be.entity.Favorite;
import com.example.be.repository.favorite.FavoriteRepository;
import com.example.be.repository.product.ProductRepository;
import com.example.be.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            UserRepository userRepository,
            ProductRepository productRepository
    ) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void like(Long userId, Long productId) {
        if (favoriteRepository.existsByUser_IdAndProduct_Id(userId, productId)) return;

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        var fav = new Favorite();
        fav.setUser(user);
        fav.setProduct(product);
        favoriteRepository.save(fav);
    }

    @Transactional
    public void unlike(Long userId, Long productId) {
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }


    @Transactional(readOnly = true)
    public List<FavoriteRepository.FavoriteRow> getMyFavorites(Long userId) {
        return favoriteRepository.findMyFavorites(userId);
    }
}
