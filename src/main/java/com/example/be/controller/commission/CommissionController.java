package com.example.be.controller.commission;

import com.example.be.dto.checkout.CheckoutRequestDTO;
import com.example.be.dto.checkout.OrderSummaryDTO;
import com.example.be.dto.commission.CommissionRequestDTO;
import com.example.be.dto.commission.CommissionRequestResponseDTO;
import com.example.be.entity.User;
import com.example.be.security.util.SecurityUtils;
import com.example.be.service.checkout.CheckoutService;
import com.example.be.service.commission.CommissionService;
import com.example.be.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commission-requests")
@RequiredArgsConstructor
public class CommissionController {

    private final CommissionService service;
    private final UserService userService;
    private final CheckoutService checkoutService;

    @GetMapping("/my")
    public List<CommissionRequestResponseDTO> myRequests() {
        User user = getCurrentUser();
        return service.getMyRequests(user);
    }

    @PostMapping
    public CommissionRequestResponseDTO create(@RequestBody CommissionRequestDTO dto) {
        User user = getCurrentUser();
        return service.create(dto, user);
    }

    @PutMapping("/{id}")
    public CommissionRequestResponseDTO updateDraft(
            @PathVariable Long id,
            @RequestBody CommissionRequestDTO dto
    ) {
        User user = getCurrentUser();
        return service.updateDraft(id, dto, user);
    }

    @PostMapping("/{id}/submit")
    public void submit(@PathVariable Long id) {
        User user = getCurrentUser();
        service.submit(id, user);
    }

    @PostMapping("/{id}/checkout")
    public OrderSummaryDTO checkout(
            @PathVariable Long id,
            @RequestBody CheckoutRequestDTO req
    ) {
        User user = getCurrentUser();
        return checkoutService.checkoutFromCommission(user.getEmail(), id, req);
    }

    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        User user = getCurrentUser();
        service.cancel(id, user);
    }

    private User getCurrentUser() {
        String email = SecurityUtils.getCurrentEmail();
        if (email == null) {
            throw new RuntimeException("Unauthenticated");
        }
        return userService.getUserEntityByEmail(email);
    }
}
