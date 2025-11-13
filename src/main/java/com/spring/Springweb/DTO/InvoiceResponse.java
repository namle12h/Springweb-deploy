/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    private Integer id;
    private Integer customerId;
    private Integer appointmentId;
    private BigDecimal subTotal;
    private BigDecimal vat;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;

    private String paymentMethod;      // Tiền mặt, QR, VNPAY, MoMo,...
    private BigDecimal amountPaid;     // Số tiền khách đưa
    private BigDecimal changeAmount;   // Tiền thừa hoặc thiếu
    private String notes;              // Ghi chú đặc biệt
    private String transactionId;      // Mã giao dịch từ cổng thanh toán
    // private List<InvoiceItemRequest> items;
    private List<InvoiceItemResponse> items;
     private String txnRef;
     private String paymentUrl;

}
