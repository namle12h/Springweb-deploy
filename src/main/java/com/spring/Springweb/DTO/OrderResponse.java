/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {

    private Integer id; // ID của Invoice/Order
    private String txnRef; // Mã giao dịch
    private String status;
    private BigDecimal total;
    private String message;
    private String paymentMethod;

    private String receiverName;
    private String receiverAddress;
    private String receiverPhone;

    private List<OrderItemResponse> orderItems;
}
