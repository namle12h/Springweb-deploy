/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceCreateRequest {

    private Integer appointmentId;
    private Integer customerId;
    private List<InvoiceItemRequest> items;
    private BigDecimal vat;           // % VAT
    private BigDecimal discountAmount;
    private String paymentMethod;
    private BigDecimal amountPaid;
    private String notes;

}
