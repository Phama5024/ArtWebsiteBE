package com.example.be.entity;

import com.example.be.enums.CommissionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String contactMethod;
    private String contactValue;

    @Enumerated(EnumType.STRING)
    private CommissionStatus status;

    private BigDecimal totalPrice;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<CommissionItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
