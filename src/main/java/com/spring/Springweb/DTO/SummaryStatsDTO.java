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
public class SummaryStatsDTO {
    
    // 1. Doanh Thu Hôm Nay / Kỳ
    private BigDecimal revenue; // Giá trị hiện tại
    private Double revenueComparisonPercent; // % so với kỳ trước (VD: +12.5)

    // 2. Lịch Hẹn Hôm Nay / Kỳ
    private BigDecimal appointments;
    private Double appointmentsComparisonPercent;
    
    // 3. Khách Hàng Mới
    private Long newCustomers;
    private Double newCustomersComparisonPercent;
    
    // 4. Tỷ Lệ Hoàn Thành
    private Double completionRate; // Giá trị là % (VD: 94.2)
    private Double completionRateComparisonPercent; // Thay đổi so với kỳ trước (VD: -2.1)
    
    // Thêm các trường ngày tháng để debug/kiểm tra dễ hơn
    private String periodLabel; // Ví dụ: "Hôm nay", "30 ngày qua"
    private String startDate;
    private String endDate;
}