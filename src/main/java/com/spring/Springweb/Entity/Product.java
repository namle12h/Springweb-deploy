package com.spring.Springweb.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "Product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Size(min = 1, max = 200)
    private String name;

    @Size(max = 50)
    private String sku;

    @NotNull
    @Size(min = 1, max = 20)
    private String uom;

    @Size(max = 100)
    private String category;

    @Size(max = 100)
    private String brand;

    @Column(length = 255)
    private String description;

    private BigDecimal costPrice;
    private BigDecimal salePrice;

    @NotNull
    private BigDecimal stockQty;

    private Integer reorderLevel;
    private LocalDate expDate;
    private Boolean active;

    @Column(length = 255)
    private String imageUrl; // ✅ Ảnh chính hiển thị thumbnail

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    private Integer createdBy;
    private Integer updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        this.active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }

    // ✅ Quan hệ 1-1 với ProductDetails
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private ProductDetails details;

    // ✅ Quan hệ 1-n với ProductImage
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ProductImage> images = new ArrayList<>();
}
