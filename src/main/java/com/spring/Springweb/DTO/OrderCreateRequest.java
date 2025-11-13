/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateRequest {

    // Thông tin giao hàng
    private String receiverName;
    private String receiverPhone;
    private String addressDetail;
    private Integer cityId;
    private String cityName;
    private String districtName;
    private String communeName;
    private String notes;

    // Chi tiết thanh toán & Giảm giá (Lấy từ frontend)
    private String paymentMethod;
//    private BigDecimal Vat;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee; // Phí ship
    private LocalDateTime createdAt;
    // Sản phẩm
    private List<ProductOrderItemRequest> items;
}
