package com.example.be.service.order;

import com.example.be.dto.order.AdminOrderDetailDTO;
import com.example.be.dto.order.AdminOrderRowDTO;
import com.example.be.dto.order.AdminOrderUpdateRequestDTO;
import com.example.be.entity.Order;
import com.example.be.enums.OrderStatus;
import com.example.be.repository.order.OrderItemRepository;
import com.example.be.repository.order.OrderRepository;
import com.example.be.repository.view.AdminOrderRowView;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;

    private final InvoicePdfGenerator invoicePdfGenerator;

    public Page<AdminOrderRowDTO> paged(int page, int size, String sort, String keyword, OrderStatus status) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), parseSort(sort));
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<AdminOrderRowView> raw = orderRepo.adminPaged(kw, status, pageable);

        return raw.map(v -> new AdminOrderRowDTO(
                v.getId(),
                buildOrderCode(v.getId()),
                v.getTransactionId(),
                v.getPaymentMethod(),
                v.getTotalAmount(),
                v.getStatus(),
                v.getCreatedAt()
        ));
    }

    public AdminOrderDetailDTO detail(Long id) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        if (Boolean.TRUE.equals(o.getDeleted())) throw new RuntimeException("Order deleted");

        var items = itemRepo.findAdminItems(id);
        var p = o.getPayment();

        return new AdminOrderDetailDTO(
                o.getId(),
                buildOrderCode(o.getId()),
                o.getCreatedAt(),
                o.getTotalAmount(),
                o.getStatus(),
                o.getReceiverName(),
                o.getReceiverPhone(),
                o.getShippingAddress(),

                p == null ? null : p.getTransactionId(), // invoiceCode bạn đang dùng transactionId
                p == null ? null : p.getPaymentMethod(),
                p == null ? null : p.getPaymentStatus(),
                p == null ? null : p.getPaidAt(),

                items
        );
    }

    public byte[] generateInvoicePdf(Long id) {
        AdminOrderDetailDTO detail = detail(id);

        var dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String createdAtText = detail.createdAt() == null ? "" : detail.createdAt().format(dtf);
        String paidAtText = detail.paidAt() == null ? "" : detail.paidAt().format(dtf);

        String totalAmountText = moneyVn(detail.totalAmount());

        List<InvoiceItemVM> items = (detail.items() == null ? List.<com.example.be.dto.order.AdminOrderItemDTO>of() : detail.items())
                .stream()
                .map(it -> {
                    String unitPriceText = moneyVn(it.unitPrice());
                    java.math.BigDecimal lineTotal = lineTotal(it.unitPrice(), it.quantity());
                    String lineTotalText = moneyVn(lineTotal);
                    return new InvoiceItemVM(
                            it.productName(),
                            it.fileFormat(),
                            it.quantity(),
                            unitPriceText,
                            lineTotalText
                    );
                })
                .toList();

        Map<String, Object> model = new HashMap<>();
        model.put("order", detail);
        model.put("createdAtText", createdAtText);
        model.put("paidAtText", paidAtText);
        model.put("totalAmountText", totalAmountText);
        model.put("items", items);

        return invoicePdfGenerator.generate("invoice", model);
    }

    private java.math.BigDecimal lineTotal(java.math.BigDecimal unitPrice, Integer qty) {
        if (unitPrice == null || qty == null) return java.math.BigDecimal.ZERO;
        return unitPrice.multiply(java.math.BigDecimal.valueOf(qty));
    }

    private String moneyVn(java.math.BigDecimal amount) {
        if (amount == null) return "";
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return nf.format(amount) + " VND";
    }

    private static record InvoiceItemVM(
            String productName,
            String fileFormat,
            Integer quantity,
            String unitPriceText,
            String lineTotalText
    ) {}



    @Transactional
    public void update(Long id, AdminOrderUpdateRequestDTO req) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        if (Boolean.TRUE.equals(o.getDeleted())) throw new RuntimeException("Order deleted");

        if (req.status() != null) o.setStatus(req.status());
        if (req.receiverName() != null) o.setReceiverName(req.receiverName());
        if (req.receiverPhone() != null) o.setReceiverPhone(req.receiverPhone());
        if (req.shippingAddress() != null) o.setShippingAddress(req.shippingAddress());
    }

    @Transactional
    public void softDeleteMany(List<Long> ids) {
        orderRepo.softDeleteMany(ids);
    }

    private String buildOrderCode(Long id) {
        return "#AA" + String.format("%08d", id);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.by(Sort.Direction.DESC, "createdAt");
        try {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            Sort.Direction dir = (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            return Sort.by(dir, field);
        } catch (Exception e) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
}
