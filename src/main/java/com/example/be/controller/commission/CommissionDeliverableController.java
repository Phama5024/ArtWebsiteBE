package com.example.be.controller.commission;

import com.example.be.dto.commission.CommissionDeliverableResponseDTO;
import com.example.be.entity.User;
import com.example.be.security.util.SecurityUtils;
import com.example.be.service.commission.CommissionDeliverableService;
import com.example.be.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commission-requests")
@RequiredArgsConstructor
public class CommissionDeliverableController {

    private final CommissionDeliverableService deliverableService;
    private final UserService userService;

    @GetMapping("/{id}/deliverables")
    public List<CommissionDeliverableResponseDTO> list(@PathVariable Long id) {
        String email = SecurityUtils.getCurrentEmail();
        if (email == null) throw new RuntimeException("Unauthenticated");

        User user = userService.getUserEntityByEmail(email);
        return deliverableService.listForUser(id, user);
    }
}
