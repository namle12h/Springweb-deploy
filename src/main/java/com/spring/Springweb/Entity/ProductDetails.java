package com.spring.Springweb.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ProductDetails")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "ProductId", nullable = false, unique = true)
    private Product product;

    @Lob
    private String detailDescription;

    @Column(length = 255)
    private String slug;

    @Column(precision = 18, scale = 2)
    private BigDecimal oldPrice;

    private Integer discountPercent;
    private Boolean isFeatured;

    @Column(length = 255)
    private String tags;

    @Column(length = 50)
    private String barcode;

    @Column(length = 100)
    private String location;

    private Boolean deleted;
}
