package com.example.be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "download_path", columnDefinition = "TEXT")
    private String downloadPath;

    @Column(name = "download_name")
    private String downloadName;

    @Column(name = "product_name_snapshot")
    private String productNameSnapshot;

    @Column(name = "thumbnail_url_snapshot", columnDefinition = "TEXT")
    private String thumbnailUrlSnapshot;

    @Column(name = "file_format_snapshot")
    private String fileFormatSnapshot;

}
