package com.example.be.controller.cart;


import com.example.be.dto.cart.AddToCartRequestDTO;
import com.example.be.dto.cart.CartDTO;
import com.example.be.dto.cart.UpdateCartItemRequestDTO;
import com.example.be.service.cart.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartDTO getMyCart(Authentication authentication) {
        String email = authentication.getName();
        return cartService.getMyCart(email);
    }

    @PostMapping("/items")
    public CartDTO addToCart(Authentication authentication,
                             @Valid @RequestBody AddToCartRequestDTO req) {
        String email = authentication.getName();
        return cartService.addToCart(email, req.getProductId(), req.getQuantity());
    }

    @PatchMapping("/items/{productId}")
    public CartDTO updateQuantity(Authentication authentication,
                                  @PathVariable Long productId,
                                  @Valid @RequestBody UpdateCartItemRequestDTO req) {
        String email = authentication.getName();
        return cartService.updateQuantity(email, productId, req.getQuantity());
    }

    @DeleteMapping("/items/{productId}")
    public void removeItem(Authentication authentication,
                           @PathVariable Long productId) {
        String email = authentication.getName();
        cartService.removeItem(email, productId);
    }
}
