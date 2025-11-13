package com.spring.Springweb.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDTO {
    private Integer id;
    private String imageUrl;
    private Boolean isPrimary;   // ✅ khớp với cột trong entity
    private Integer sortOrder;   // ✅ khớp với cột trong entity
}
