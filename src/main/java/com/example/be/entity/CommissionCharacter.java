package com.example.be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "commission_characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommissionCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "commission_item_id")
    private CommissionItem commissionItem;

    private Integer characterIndex;
    private String poseScope;
    private BigDecimal extraPrice;
}
