package com.example.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_meta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    private String fileFormat;
    private String resolution;
    private String aspectRatio;
    private String fileSize;
    private String author;
    private String style;
    private String origin;

    @Column(name = "character_name")
    private String characterName;

    @Column(name = "extra_info", columnDefinition = "TEXT")
    private String extraInfo;

    @Column(name = "file_url", columnDefinition = "TEXT")
    private String fileUrl;

    @Column(name = "download_path", columnDefinition = "TEXT")
    private String downloadPath;

    @Column(name = "download_name")
    private String downloadName;

    @Column(name = "download_mime")
    private String downloadMime;

    @Column(name = "download_size")
    private Long downloadSize;
}
