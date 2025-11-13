/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProductUsageDTO {

    private Integer id;
    private Integer serviceId;
    private Integer productId;
    private String note;
    private Integer sortOrder;

    // Thông tin từ bảng Product để hiển thị
    private String productName;
    private String brand;
    private String category;
    private BigDecimal salePrice;
    private String imageUrl;
}
