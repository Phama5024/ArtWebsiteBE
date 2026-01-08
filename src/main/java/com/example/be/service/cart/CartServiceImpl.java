package com.example.be.service.cart;


import com.example.be.dto.cart.CartDTO;
import com.example.be.dto.cart.CartItemDTO;
import com.example.be.entity.Cart;
import com.example.be.entity.CartItem;
import com.example.be.entity.Product;
import com.example.be.entity.User;
import com.example.be.repository.cart.CartItemRepository;
import com.example.be.repository.cart.CartRepository;
import com.example.be.repository.product.ProductRepository;
import com.example.be.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CartDTO getMyCart(String email) {
        return cartRepository.findByUserEmail(email)
                .map(this::toDTO)
                .orElseGet(() -> {
                    CartDTO dto = new CartDTO();
                    dto.setCartId(null);
                    dto.setItems(List.of());
                    dto.setTotalAmount(BigDecimal.ZERO);
                    return dto;
                });
    }

    @Override
    public CartDTO addToCart(String email, Long productId, Integer quantity) {
        int qty = (quantity == null || quantity <= 0) ? 1 : quantity;

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (Boolean.TRUE.equals(product.getDeleted())) {
            throw new RuntimeException("Product is not available");
        }

        String status = product.getStatus();
        if (status == null || status.isBlank()) {
            throw new RuntimeException("Product is not available");
        }

        if ("OUT_OF_STOCK".equalsIgnoreCase(status.trim())) {
            throw new RuntimeException("Product is not available");
        }

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUser(user);
                    c.setItems(new ArrayList<>());
                    return cartRepository.save(c);
                });

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setCart(cart);
                    ci.setProduct(product);
                    ci.setQuantity(0);
                    return ci;
                });

        item.setQuantity(item.getQuantity() + qty);
        cartItemRepository.save(item);

        return toDTO(cart);
    }

    @Override
    public CartDTO updateQuantity(String email, Long productId, Integer quantity) {
        if (quantity == null) throw new RuntimeException("quantity is required");

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return toDTO(cart);
    }

    @Override
    public void removeItem(String email, Long productId) {
        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
    }

    private CartDTO toDTO(Cart cart) {
        List<CartItem> items = cartItemRepository.findAllByCartIdFetchProductAndMedia(cart.getId());

        List<CartItemDTO> itemDTOs = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : items) {
            Product p = ci.getProduct();
            BigDecimal price = p.getPrice();
            int qty = ci.getQuantity() == null ? 0 : ci.getQuantity();

            String imageUrl = null;
            if (p.getMediaList() != null && !p.getMediaList().isEmpty()) {
                imageUrl = p.getMediaList().stream()
                        .filter(m -> Boolean.TRUE.equals(m.getIsPrimary()))
                        .map(m -> m.getImageUrl())
                        .findFirst()
                        .orElse(p.getMediaList().get(0).getImageUrl());
            }

            CartItemDTO dto = new CartItemDTO();
            dto.setProductId(p.getId());
            dto.setProductName(p.getName());
            dto.setPrice(price);
            dto.setQuantity(qty);
            dto.setImageUrl(imageUrl);

            itemDTOs.add(dto);

            if (price != null) {
                total = total.add(price.multiply(BigDecimal.valueOf(qty)));
            }
        }


        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(cart.getId());
        cartDTO.setItems(itemDTOs);
        cartDTO.setTotalAmount(total);
        return cartDTO;
    }
}
