package com.example.be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "commission_deliverables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommissionDeliverable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "commission_request_id")
    private CommissionRequest commissionRequest;

    @Column(nullable = false, length = 500)
    private String filePath;

    private String originalName;
    private String contentType;
    private Long size;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
