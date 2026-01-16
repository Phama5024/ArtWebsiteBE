package com.example.be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commission_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommissionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private CommissionRequest request;

    private String style;
    private BigDecimal basePrice;

    @OneToMany(mappedBy = "commissionItem", cascade = CascadeType.ALL)
    private List<CommissionCharacter> characters = new ArrayList<>();
}
