package com.spring.Springweb.DTO;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Integer id;
    private String name;
    private String sku;
    private String uom;
    // private String category;
    private String brand;
    private String description;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
    private BigDecimal discountPrice;    // 🔥 giá khuyến mãi
    private Integer discountPercent;     // 🔥 % giảm
        // ===== THỜI TRANG =====
    private String size;                 // S, M, L, XL
    private String color;                // Đen, Trắng, Xanh

    // ===== KHUYẾN MÃI =====
    private LocalDate discountStartDate;
    private LocalDate discountEndDate;
    private BigDecimal stockQty;
    private Integer reorderLevel;
    private LocalDate expDate;
    private Boolean active;
    private String imageUrl; // ảnh chính

    private Integer createdBy;
     private String createdByName;
    private Integer updatedBy;
    private String updatedByName;
    private Integer categoryId;
    private ProductDetailsDTO details; // ✅ liên kết 1-1
    private List<ProductImageDTO> images; // ✅ liên kết 1-n


}
