/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ProductOrderItemRequest {

    private Integer productId;
    private Integer quantity;
    @JsonProperty("price")
    private BigDecimal pricePerUnit;
    // Thêm các trường khác cần thiết như name, imageUrl nếu muốn lưu vào InvoiceItem
}
