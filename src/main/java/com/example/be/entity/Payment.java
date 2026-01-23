package com.example.be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "payment_method")
    private String paymentMethod;
    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "provider_ref")
    private String providerRef;

    @Lob
    @Column(name = "provider_payload", columnDefinition = "LONGTEXT")
    private String providerPayload;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
