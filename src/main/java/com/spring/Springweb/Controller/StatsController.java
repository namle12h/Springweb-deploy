/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.Springweb.DTO.AppointmentStatsDTO;
import com.spring.Springweb.DTO.CustomerStatsDTO;
import com.spring.Springweb.DTO.PerformanceStatsDTO;
import com.spring.Springweb.DTO.RevenueAnalysisResponse;
import com.spring.Springweb.DTO.RevenueTrendDTO;
import com.spring.Springweb.DTO.ServiceBreakdownDTO;
import com.spring.Springweb.DTO.SummaryStatsDTO;
import com.spring.Springweb.Service.StatsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/revenue-trend")
    public ResponseEntity<List<RevenueTrendDTO>> getRevenueTrend(
            @RequestParam(defaultValue = "last_30_days") String period,
            @RequestParam(required = false) Integer locationId
    ) {
        List<RevenueTrendDTO> data = statsService.getRevenueTrendData(period);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/service-breakdown")
    public ResponseEntity<List<ServiceBreakdownDTO>> getServiceBreakdown(
            @RequestParam(defaultValue = "last_30_days") String period,
            @RequestParam(required = false) Integer locationId
    ) {
        List<ServiceBreakdownDTO> data = statsService.getServiceBreakdown(period);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/appointment-stats")
    public ResponseEntity<AppointmentStatsDTO> getAppointmentStats(
            @RequestParam(defaultValue = "today") String period,
            @RequestParam(required = false) Integer locationId,
            @RequestParam(required = false) String startDate, // Thêm startDate
            @RequestParam(required = false) String endDate
    ) {
        AppointmentStatsDTO data = statsService.getAppointmentStats(period, locationId, startDate, endDate);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/overview")
    public ResponseEntity<SummaryStatsDTO> getSummaryStats(
            @RequestParam(required = false, defaultValue = "today") String period,
            @RequestParam(required = false) String startDate, // Thêm startDate
            @RequestParam(required = false) String endDate) {
        SummaryStatsDTO stats = statsService.getSummaryData(period, startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/customers")
    public CustomerStatsDTO getCustomerStats(@RequestParam(required = false, defaultValue = "last_30_days") String period,
            @RequestParam(required = false) String startDate, // Thêm startDate
            @RequestParam(required = false) String endDate) {
        return statsService.getCustomerStats(period, startDate, endDate);
    }

    @GetMapping("/performance")
    public ResponseEntity<PerformanceStatsDTO> getPerformanceStats(
            @RequestParam(value = "period", defaultValue = "last_30_days") String period,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {

        // Gọi service để lấy dữ liệu thống kê hiệu suất
        PerformanceStatsDTO performanceStats = statsService.getPerformanceStats(period, startDate, endDate);

        // Trả về dữ liệu dưới dạng ResponseEntity (JSON)
        return ResponseEntity.ok(performanceStats);
    }

    @GetMapping("/revenue-analysis")
    public RevenueAnalysisResponse getRevenueAnalysis(
            @RequestParam(defaultValue = "month") String mode,
            @RequestParam(required = false) Integer year
    ) {
        return statsService.getRevenueAnalysis(mode, year);
    }

    @GetMapping("/category-summary")
    public ResponseEntity<?> getCategorySummary() {
        return ResponseEntity.ok(statsService.getCategorySummary());
    }

}
