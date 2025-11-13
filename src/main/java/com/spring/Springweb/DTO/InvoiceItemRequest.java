/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItemRequest {

    private Integer serviceId;   // nếu là dịch vụ
    private Integer productId;   // nếu là sản phẩm
    private Integer quantity;
    private BigDecimal unitPrice;
    
}
