package com.example.be.service.commission;

import com.example.be.dto.commission.CommissionDeliverableResponseDTO;
import com.example.be.entity.CommissionDeliverable;
import com.example.be.entity.CommissionRequest;
import com.example.be.entity.User;
import com.example.be.enums.CommissionStatus;
import com.example.be.enums.NotificationType;
import com.example.be.repository.commission.CommissionDeliverableRepository;
import com.example.be.repository.commission.CommissionRequestRepository;
import com.example.be.service.notifications.NotificationService;
import com.example.be.service.storage.CommissionFileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommissionDeliverableService {

    private final CommissionRequestRepository requestRepo;
    private final CommissionDeliverableRepository deliverableRepo;
    private final CommissionFileStorage storage;
    private final NotificationService notificationService;

    public CommissionDeliverableResponseDTO uploadBySeller(Long requestId, User seller, MultipartFile file) throws Exception {
        checkSeller(seller);

        CommissionRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commission not found"));

        ensureSellerAssigned(req, seller);
        assertNotLocked(req);

        validateImage(file);

        String path = storage.save(requestId, file);

        CommissionDeliverable d = new CommissionDeliverable();
        d.setCommissionRequest(req);
        d.setFilePath(path);
        d.setOriginalName(file.getOriginalFilename());
        d.setContentType(file.getContentType());
        d.setSize(file.getSize());

        d = deliverableRepo.save(d);
        return toDto(d);
    }

    public void deleteBySeller(Long requestId, Long deliverableId, User seller) {
        checkSeller(seller);

        CommissionRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commission not found"));

        assertSellerOwnsRequest(req, seller);
        assertNotLocked(req);

        CommissionDeliverable d = deliverableRepo.findByIdAndCommissionRequestId(deliverableId, requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deliverable not found"));

        storage.deleteByPublicPath(d.getFilePath());
        deliverableRepo.delete(d);
    }

    public List<CommissionDeliverableResponseDTO> listForUser(Long requestId, User user) {
        CommissionRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commission not found"));

        if (!req.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        return deliverableRepo.findByCommissionRequestIdOrderByCreatedAtDesc(requestId)
                .stream().map(this::toDto).toList();
    }

    public List<CommissionDeliverableResponseDTO> listForSeller(Long requestId, User seller) {
        checkSeller(seller);

        CommissionRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commission not found"));

        assertSellerOwnsRequest(req, seller);

        return deliverableRepo.findByCommissionRequestIdOrderByCreatedAtDesc(requestId)
                .stream().map(this::toDto).toList();
    }

    private void ensureSellerAssigned(CommissionRequest req, User seller) {
        if (req.getSeller() == null) {
            req.setSeller(seller);
            requestRepo.save(req);
            return;
        }
        if (!req.getSeller().getId().equals(seller.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden: not this commission's seller");
        }
    }

    private void assertSellerOwnsRequest(CommissionRequest req, User seller) {
        if (req.getSeller() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden: commission not assigned to a seller yet");
        }
        if (!req.getSeller().getId().equals(seller.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden: not this commission's seller");
        }
    }

    private void assertNotLocked(CommissionRequest req) {
        if (req.getStatus() == CommissionStatus.CONFIRMED || req.getStatus() == CommissionStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Locked: cannot modify deliverables after CONFIRMED/PAID");
        }
    }

    private CommissionDeliverableResponseDTO toDto(CommissionDeliverable d) {
        CommissionDeliverableResponseDTO dto = new CommissionDeliverableResponseDTO();
        dto.setId(d.getId());
        dto.setFileUrl(d.getFilePath());
        dto.setOriginalName(d.getOriginalName());
        dto.setContentType(d.getContentType());
        dto.setSize(d.getSize());
        dto.setCreatedAt(d.getCreatedAt());
        return dto;
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }
    }

    private void checkSeller(User user) {
        boolean isSeller = user.getRoles().stream().anyMatch(r -> "SELLER".equals(r.getName()));
        if (!isSeller) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden: not seller");
        }
    }
}
