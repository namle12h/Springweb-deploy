package com.spring.Springweb.DTO;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailsDTO {
    private Integer id;
    private String detailDescription;
    private String slug;
    private BigDecimal oldPrice;
    private Integer discountPercent;
    private Boolean isFeatured;
    private String tags;
    private String barcode;
    private String location;
    private Boolean deleted;
    private List<ProductImageDTO> images;
}
