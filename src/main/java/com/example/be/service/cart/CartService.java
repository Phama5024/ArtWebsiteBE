package com.example.be.service.cart;


import com.example.be.dto.cart.CartDTO;

public interface CartService {
    CartDTO getMyCart(String email);
    CartDTO addToCart(String email, Long productId, Integer quantity);
    CartDTO updateQuantity(String email, Long productId, Integer quantity);
    void removeItem(String email, Long productId);
}
