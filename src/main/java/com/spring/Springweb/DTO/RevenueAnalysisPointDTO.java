package com.spring.Springweb.DTO;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevenueAnalysisPointDTO {
    private String label;      // T1, T2, Q1, 2025,...
    private BigDecimal real;   // doanh thu thực tế
    private BigDecimal target; // doanh thu mục tiêu
}
