package com.spring.Springweb.DTO;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevenueAnalysisResponse {
    private String mode;                  // month | quarter | year
    private Integer year;
    private List<RevenueAnalysisPointDTO> chartData;

    private BigDecimal totalRevenue;      // tổng doanh thu
    private Double growthPercent;         // % tăng trưởng
    private Double targetAchieved;        // % đạt mục tiêu
    private List<RevenueTrendDTO> revenueTrends; 
    private List<TopServiceDTO> topServices; 
}
