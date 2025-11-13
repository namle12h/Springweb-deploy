/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import java.util.List;

import com.spring.Springweb.DTO.AppointmentStatsDTO;
import com.spring.Springweb.DTO.CategoryStatDTO;
import com.spring.Springweb.DTO.CustomerStatsDTO;
import com.spring.Springweb.DTO.PerformanceStatsDTO;
import com.spring.Springweb.DTO.RevenueAnalysisResponse;
import com.spring.Springweb.DTO.RevenueTrendDTO;
import com.spring.Springweb.DTO.ServiceBreakdownDTO;
import com.spring.Springweb.DTO.SummaryStatsDTO;
import com.spring.Springweb.DTO.TopServiceDTO;
import java.time.LocalDateTime;
import java.util.Map;

public interface StatsService {

    SummaryStatsDTO getSummaryData(String period, String startDate, String endDate);

    List<RevenueTrendDTO> getRevenueTrendData(String period);

    List<ServiceBreakdownDTO> getServiceBreakdown(String period);

    AppointmentStatsDTO getAppointmentStats(String period, Integer locationId, String startDate, String endDate);

    CustomerStatsDTO getCustomerStats(String period, String startDate, String endDate);

    public PerformanceStatsDTO getPerformanceStats(String period, String startDate, String endDate) ;
    
    RevenueAnalysisResponse getRevenueAnalysis(String mode, Integer year);
    
    List<TopServiceDTO> getTopServices(LocalDateTime start, LocalDateTime end);

    Map<String, CategoryStatDTO> getCategorySummary();


}
