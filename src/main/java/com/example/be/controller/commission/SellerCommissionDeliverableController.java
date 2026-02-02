package com.example.be.controller.commission;

import com.example.be.dto.commission.CommissionDeliverableResponseDTO;
import com.example.be.entity.User;
import com.example.be.security.util.SecurityUtils;
import com.example.be.service.commission.CommissionDeliverableService;
import com.example.be.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/seller/commissions")
@RequiredArgsConstructor
public class SellerCommissionDeliverableController {

    private final CommissionDeliverableService deliverableService;
    private final UserService userService;

    @PostMapping(value = "/{id}/deliverables", consumes = {"multipart/form-data"})
    public CommissionDeliverableResponseDTO upload(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        User seller = getCurrentUser();
        return deliverableService.uploadBySeller(id, seller, file);
    }

    @GetMapping("/{id}/deliverables")
    public List<CommissionDeliverableResponseDTO> list(@PathVariable Long id) {
        User seller = getCurrentUser();
        return deliverableService.listForSeller(id, seller);
    }

    @DeleteMapping("/{id}/deliverables/{deliverableId}")
    public void delete(
            @PathVariable Long id,
            @PathVariable Long deliverableId
    ) {
        User seller = getCurrentUser();
        deliverableService.deleteBySeller(id, deliverableId, seller);
    }

    private User getCurrentUser() {
        String email = SecurityUtils.getCurrentEmail();
        if (email == null) throw new RuntimeException("Unauthenticated");
        return userService.getUserEntityByEmail(email);
    }
}
