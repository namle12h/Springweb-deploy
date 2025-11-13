/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopServiceDTO {
    private String name;          // Tên dịch vụ
    private Long appointments;    // Số lượt đặt
    private Long revenue;         // Tổng doanh thu theo hóa đơn
    private Double change;        // % thay đổi so với kỳ trước
    private boolean up;           // Tăng / giảm
}
