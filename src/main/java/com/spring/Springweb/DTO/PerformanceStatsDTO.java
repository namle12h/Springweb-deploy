package com.spring.Springweb.DTO;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PerformanceStatsDTO {

    private BigDecimal serviceRevenue; // Doanh thu từ dịch vụ
    private BigDecimal productRevenue; // Doanh thu từ sản phẩm
    private BigDecimal totalRevenue; // Tổng doanh thu
    private String revenueChangePercent; // Phần trăm thay đổi doanh thu
    private BigDecimal averageRating; // Đánh giá trung bình
    private BigDecimal customerCount; // Số lượng khách hàng mới
    private Double completionRate; // Tỷ lệ hoàn thành
}
