/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

// com.spring.Springweb.DTO.OrderItemResponse.java

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {
    private Integer productId;
    private String productName;
    private String brand;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal price;
}
