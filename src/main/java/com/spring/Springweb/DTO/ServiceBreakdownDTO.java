/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceBreakdownDTO {

    private String serviceName;
    private BigDecimal revenueShare; // Tỷ lệ phần trăm doanh thu (VD: 35.5)
    private  BigDecimal totalAppointments; // Tổng số lịch hẹn cho dịch vụ này

}
