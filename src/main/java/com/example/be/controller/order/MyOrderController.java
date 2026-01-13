package com.example.be.controller.order;

import com.example.be.dto.order.MyOrderDTO;
import com.example.be.entity.Order;
import com.example.be.entity.OrderItem;
import com.example.be.enums.OrderStatus;
import com.example.be.repository.order.OrderItemRepository;
import com.example.be.repository.order.OrderRepository;
import com.example.be.service.order.MyOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class MyOrderController {

    private final MyOrderService myOrderService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @GetMapping("/my")
    public Page<MyOrderDTO> myOrders(
            Authentication authentication,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable
    ) {
        String email = authentication.getName();
        return myOrderService.myOrders(email, status, pageable);
    }

    @GetMapping("/my/{orderId}")
    public MyOrderDTO myOrderDetail(Authentication authentication, @PathVariable Long orderId) {
        String email = authentication.getName();
        return myOrderService.myOrderDetail(email, orderId);
    }
    @GetMapping("/my/{orderId}/items/{itemId}/download")
    public ResponseEntity<Resource> downloadMyItem(
            Authentication authentication,
            @PathVariable Long orderId,
            @PathVariable Long itemId
    ) throws Exception {
        String email = authentication.getName();

        Order order = orderRepository.findOwnedOrderFetch(orderId, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order cancelled");
        }

        if (order.getPayment() == null
                || order.getPayment().getPaymentStatus() == null
                || !"PAID".equalsIgnoreCase(order.getPayment().getPaymentStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not paid");
        }

        OrderItem item = orderItemRepository.findByIdAndOrderId(itemId, orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        String relPath = item.getDownloadPath();
        if (relPath == null || relPath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No digital file");
        }

        Path base = Paths.get("uploads").toAbsolutePath().normalize();
        Path target = base.resolve(relPath).normalize();

        if (!target.startsWith(base)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }
        if (!Files.exists(target)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }

        Resource resource = new UrlResource(target.toUri());

        String filename = (item.getDownloadName() != null && !item.getDownloadName().isBlank())
                ? item.getDownloadName()
                : target.getFileName().toString();

        String contentType;
        try {
            contentType = Files.probeContentType(target);
        } catch (Exception e) {
            contentType = null;
        }
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

}
