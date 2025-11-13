package com.spring.Springweb.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryStatDTO {
    private MetricDTO today;
    private MetricDTO week;
    private MetricDTO month;
    private MetricDTO quarter;
}
