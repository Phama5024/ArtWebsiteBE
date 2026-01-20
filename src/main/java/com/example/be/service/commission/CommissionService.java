package com.example.be.service.commission;

import com.example.be.dto.commission.*;
import com.example.be.entity.*;
import com.example.be.enums.CommissionStatus;
import com.example.be.enums.NotificationType;
import com.example.be.repository.commission.CommissionRequestRepository;
import com.example.be.repository.order.OrderRepository;
import com.example.be.repository.payment.PaymentRepository;
import com.example.be.service.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommissionService {

    private final CommissionRequestRepository requestRepo;
    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final NotificationService notificationService;

    public CommissionRequestResponseDTO create(
            CommissionRequestDTO dto,
            User user
    ) {
        CommissionRequest req = new CommissionRequest();
        req.setUser(user);
        req.setTitle(dto.getTitle());
        req.setDescription(dto.getDescription());

        req.setContactMethod(dto.getContactMethod());
        req.setContactValue(dto.getContactValue());

        req.setStatus(CommissionStatus.DRAFT);

        BigDecimal total = BigDecimal.ZERO;

        for (CommissionItemDTO itemDTO : dto.getItems()) {

            CommissionItem item = new CommissionItem();
            item.setRequest(req);
            item.setStyle(itemDTO.getStyle());
            item.setBasePrice(itemDTO.getBasePrice());

            total = total.add(itemDTO.getBasePrice());

            for (CommissionCharacterDTO charDTO : itemDTO.getCharacters()) {

                CommissionCharacter c = new CommissionCharacter();
                c.setCommissionItem(item);
                c.setCharacterIndex(charDTO.getCharacterIndex());
                c.setPoseScope(charDTO.getPoseScope());
                c.setExtraPrice(charDTO.getExtraPrice());

                total = total.add(charDTO.getExtraPrice());
                item.getCharacters().add(c);
            }

            req.getItems().add(item);
        }

        req.setTotalPrice(total);

        return toResponse(requestRepo.save(req));
    }

    public void submit(Long id, User user) {
        CommissionRequest req = getOwnedRequest(id, user);

        if (req.getStatus() != CommissionStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT can be submitted");
        }

        req.setStatus(CommissionStatus.SUBMITTED);
        requestRepo.save(req);
    }

    public Order checkout(Long id, User user) {
        CommissionRequest req = getOwnedRequest(id, user);

        if (req.getStatus() != CommissionStatus.APPROVED) {
            throw new RuntimeException("Commission not approved");
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(req.getTotalPrice());
        order.setCommissionRequest(req);
        orderRepo.save(order);

        Payment payment = new Payment();
        payment.setOrder(order);
        paymentRepo.save(payment);

        req.setStatus(CommissionStatus.CONFIRMED);
        requestRepo.save(req);

        notificationService.create(
                user.getId(),
                null,
                NotificationType.ORDER_CREATED,
                "Tạo đơn hàng thành công",
                "Bạn đã tạo đơn " + order.getId() + " từ commission.",
                "/orders/" + order.getId(),
                """
                {"orderId":%d,"commissionRequestId":%d}
                """.formatted(order.getId(), req.getId())
        );


        return order;
    }

    public void cancel(Long id, User user) {
        CommissionRequest req = getOwnedRequest(id, user);

        if (req.getStatus() == CommissionStatus.CONFIRMED ||
                req.getStatus() == CommissionStatus.PAID) {
            throw new RuntimeException("Cannot cancel");
        }

        req.setStatus(CommissionStatus.CANCELLED);
        requestRepo.save(req);
    }

    public List<CommissionRequestResponseDTO> getMyRequests(User user) {
        return requestRepo.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CommissionRequestResponseDTO> getPendingForSeller(User seller) {
        checkSeller(seller);

        return requestRepo.findByStatus(CommissionStatus.SUBMITTED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void approve(Long id, BigDecimal finalPrice, User seller) {
        checkSeller(seller);

        CommissionRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission not found"));

        if (req.getStatus() != CommissionStatus.SUBMITTED) {
            throw new RuntimeException("Invalid status");
        }

        req.setSeller(seller);
        req.setTotalPrice(finalPrice);
        req.setStatus(CommissionStatus.APPROVED);

        requestRepo.save(req);

        notificationService.create(
                req.getUser().getId(),
                seller.getId(),
                NotificationType.COMMISSION_STATUS_CHANGED,
                "Commission được duyệt",
                "Yêu cầu " + req.getId() + " đã được duyệt. Giá cuối: " + finalPrice,
                "/commissions/" + req.getId(),
                """
                {"commissionRequestId":%d,"status":"%s"}
                """.formatted(req.getId(), req.getStatus().name())
        );
    }

    public void reject(Long id, User seller) {
        checkSeller(seller);

        CommissionRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission not found"));

        if (req.getStatus() != CommissionStatus.SUBMITTED) {
            throw new RuntimeException("Invalid status");
        }

        req.setSeller(seller);
        req.setStatus(CommissionStatus.REJECTED);

        requestRepo.save(req);

        notificationService.create(
                req.getUser().getId(),
                seller.getId(),
                NotificationType.COMMISSION_STATUS_CHANGED,
                "Commission bị từ chối",
                "Yêu cầu " + req.getId() + " đã bị từ chối.",
                "/commissions/" + req.getId(),
                """
                {"commissionRequestId":%d,"status":"%s"}
                """.formatted(req.getId(), req.getStatus().name())
        );

    }

    private boolean isSeller(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> "SELLER".equals(r.getName()));
    }

    private void checkSeller(User user) {
        if (!isSeller(user)) {
            throw new RuntimeException("Forbidden: not seller");
        }
    }

    private CommissionRequest getOwnedRequest(Long id, User user) {
        CommissionRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission not found"));

        if (!req.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Forbidden");
        }
        return req;
    }

    private CommissionRequestResponseDTO toResponse(CommissionRequest req) {

        CommissionRequestResponseDTO res = new CommissionRequestResponseDTO();
        res.setId(req.getId());
        res.setTitle(req.getTitle());
        res.setDescription(req.getDescription());
        res.setStatus(req.getStatus());
        res.setTotalPrice(req.getTotalPrice());
        res.setCreatedAt(req.getCreatedAt());

        res.setContactMethod(req.getContactMethod());
        res.setContactValue(req.getContactValue());

        res.setItems(
                req.getItems().stream().map(item -> {
                    CommissionItemResponseDTO i = new CommissionItemResponseDTO();
                    i.setStyle(item.getStyle());
                    i.setBasePrice(item.getBasePrice());
                    i.setCharacters(
                            item.getCharacters().stream().map(c -> {
                                CommissionCharacterResponseDTO cr =
                                        new CommissionCharacterResponseDTO();
                                cr.setCharacterIndex(c.getCharacterIndex());
                                cr.setPoseScope(c.getPoseScope());
                                cr.setExtraPrice(c.getExtraPrice());
                                return cr;
                            }).toList()
                    );
                    return i;
                }).toList()
        );

        return res;
    }
}
