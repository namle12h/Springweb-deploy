/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentStatsDTO {

    private BigDecimal totalAppointments; // Tổng số cuộc hẹn
    private BigDecimal completedAppointments; // Tổng số cuộc hẹn đã hoàn thành
    private BigDecimal completionRate; // Tỷ lệ hoàn thành (%)
    private BigDecimal totalRevenue; // Tổng doanh thu từ các cuộc hẹn đã hoàn thành
    private BigDecimal growthRate; // Tỷ lệ tăng trưởng (nếu có)
    private List<ServiceBreakdownDTO> appointmentsByService; // Chi tiết theo dịch vụ
    private Map<String, Long> appointmentsByStaff; // Thống kê theo nhân viên
    private List<DailyStatDTO> dailyStats;    // Thống kê theo ngày trong tuần
    private List<TimeSlotStatDTO> timeSlotStats;
}
