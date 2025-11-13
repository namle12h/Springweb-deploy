/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.spring.Springweb.DTO.AppointmentStatsDTO;
import com.spring.Springweb.DTO.CategoryStatDTO;
import com.spring.Springweb.DTO.CustomerStatsDTO;
import com.spring.Springweb.DTO.DailyStatDTO;
import com.spring.Springweb.DTO.MetricDTO;
import com.spring.Springweb.DTO.PerformanceStatsDTO;
import com.spring.Springweb.DTO.RevenueAnalysisPointDTO;
import com.spring.Springweb.DTO.RevenueAnalysisResponse;
import com.spring.Springweb.DTO.RevenueTrendDTO;
import com.spring.Springweb.DTO.ServiceBreakdownDTO;
import com.spring.Springweb.DTO.SummaryStatsDTO;
import com.spring.Springweb.DTO.TimeSlotStatDTO;
import com.spring.Springweb.DTO.TopServiceDTO;
import com.spring.Springweb.Repository.AppointmentRepository;
import com.spring.Springweb.Repository.InvoiceRepository;
import com.spring.Springweb.Repository.ReviewRepository;
import com.spring.Springweb.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository; // Dùng để tính Khách hàng mới
    private final InvoiceRepository invoiceRepository;
    private final ReviewRepository reviewRepository;

    private LocalDateTime[] getPeriodDates(String period) {
        LocalDate now = LocalDate.now();
        LocalDateTime currentStart, currentEnd, previousStart, previousEnd;

        switch (period.toLowerCase()) {
            case "today":
                // Kỳ hiện tại: Hôm nay
                currentStart = now.atStartOfDay();
                currentEnd = now.atTime(LocalTime.MAX);
                // Kỳ so sánh: Hôm qua
                previousStart = now.minusDays(1).atStartOfDay();
                previousEnd = now.minusDays(1).atTime(LocalTime.MAX);
                break;
            case "last_7_days":
                // Kỳ hiện tại: 7 ngày gần nhất (bao gồm hôm nay)
                currentStart = now.minusDays(6).atStartOfDay(); // 7 ngày tính cả hôm nay
                currentEnd = now.atTime(LocalTime.MAX);
                // Kỳ so sánh: 7 ngày trước đó
                previousStart = now.minusDays(13).atStartOfDay();
                previousEnd = now.minusDays(7).atTime(LocalTime.MAX);
                break;
            case "last_30_days":
            default:
                // Kỳ hiện tại: 30 ngày gần nhất
                currentStart = now.minusDays(29).atStartOfDay();
                currentEnd = now.atTime(LocalTime.MAX);
                // Kỳ so sánh: 30 ngày trước đó
                previousStart = now.minusDays(59).atStartOfDay();
                previousEnd = now.minusDays(30).atTime(LocalTime.MAX);
                break;
        }

        return new LocalDateTime[]{currentStart, currentEnd, previousStart, previousEnd};
    }

    // ===================================================
    // 1. TRIỂN KHAI API: SummaryStatsDTO
    // ===================================================
    @Override
    public SummaryStatsDTO getSummaryData(String period, String startDate, String endDate) {
        // Nếu có startDate và endDate, sử dụng chúng; nếu không, sử dụng giá trị mặc định từ getPeriodDates
        LocalDateTime currentStart, currentEnd, previousStart, previousEnd;

        // Chuyển startDate và endDate từ chuỗi thành LocalDateTime
        if (startDate != null && endDate != null) {
            currentStart = LocalDateTime.parse(startDate + "T00:00:00"); // Chuyển startDate thành LocalDateTime
            currentEnd = LocalDateTime.parse(endDate + "T23:59:59");

            // Tính toán các giá trị trước (sử dụng giá trị từ currentStart và currentEnd để xác định kỳ trước)
            previousStart = currentStart.minusDays(currentStart.getDayOfMonth());
            previousEnd = currentEnd.minusDays(currentEnd.getDayOfMonth());
        } else {
            // Nếu không có startDate và endDate, sử dụng mặc định từ getPeriodDates
            LocalDateTime[] dates = getPeriodDates(period);
            currentStart = dates[0];
            currentEnd = dates[1];
            previousStart = dates[2];
            previousEnd = dates[3];
        }

        // 1. Tính toán cho Kỳ Hiện Tại
        SummaryStatsDTO currentData = calculateStats(currentStart, currentEnd);

        // 2. Tính toán cho Kỳ So Sánh (cần thiết để tính %)
        SummaryStatsDTO previousData = calculateStats(previousStart, previousEnd);

        // Kiểm tra null cho revenue, nếu là null thì gán giá trị mặc định là 0
        BigDecimal currentRevenue = currentData.getRevenue() != null ? currentData.getRevenue() : BigDecimal.ZERO;
        BigDecimal previousRevenue = previousData.getRevenue() != null ? previousData.getRevenue() : BigDecimal.ZERO;

        // 3. Ghép và tính toán % thay đổi
        return SummaryStatsDTO.builder()
                .revenue(currentRevenue)
                .revenueComparisonPercent(calculateChange(currentRevenue.doubleValue(), previousRevenue.doubleValue()))
                .appointments(currentData.getAppointments())
                .appointmentsComparisonPercent(calculateChange(currentData.getAppointments().doubleValue(), previousData.getAppointments().doubleValue()))
                .newCustomers(currentData.getNewCustomers())
                .newCustomersComparisonPercent(calculateChange(currentData.getNewCustomers().doubleValue(), previousData.getNewCustomers().doubleValue()))
                .completionRate(currentData.getCompletionRate())
                .completionRateComparisonPercent(calculateRateChange(currentData.getCompletionRate(), previousData.getCompletionRate()))
                .periodLabel(mapPeriodToLabel(period))
                .startDate(currentStart.toLocalDate().toString()) // Bổ sung startDate
                .endDate(currentEnd.toLocalDate().toString()) // Bổ sung endDate
                .build();
    }

    private SummaryStatsDTO calculateStats(LocalDateTime start, LocalDateTime end) {
        // Truy vấn tổng doanh thu từ cơ sở dữ liệu
        BigDecimal totalRevenue = invoiceRepository.sumPaidInvoices(start, end);

        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        // Truy vấn tổng số cuộc hẹn từ cơ sở dữ liệu
        BigDecimal totalAllAppts = appointmentRepository.countAllAppointments(start, end);

        // Truy vấn tổng số cuộc hẹn đã hoàn thành
        BigDecimal totalCompletedAppts = appointmentRepository.countCompletedAppointments(start, end);

        // Tỷ lệ hoàn thành (công thức: totalCompletedAppts / totalAllAppts * 100)
        double completionRate = (totalAllAppts.compareTo(BigDecimal.ZERO) == 0) ? 0.0
                : totalCompletedAppts.multiply(new BigDecimal(100)).divide(totalAllAppts, 2, RoundingMode.HALF_UP).doubleValue();

        // Truy vấn số khách hàng mới từ cơ sở dữ liệu
        long newCustomers = userRepository.countNewCustomers(start, end);

        // Trả về SummaryStatsDTO với dữ liệu thực tế
        return SummaryStatsDTO.builder()
                .revenue(totalRevenue)
                .appointments(totalAllAppts)
                .newCustomers(newCustomers)
                .completionRate(completionRate)
                .build();
    }

    private Double calculateChange(Double current, Double previous) {
        if (previous == 0.0) {
            return (current > 0.0) ? 100.0 : 0.0; // Nếu kỳ trước bằng 0, kỳ này có > 0 thì tăng 100%
        }
        double change = ((current - previous) / previous) * 100.0;
        return Math.round(change * 10.0) / 10.0; // Làm tròn 1 chữ số thập phân
    }

    private Double calculateRateChange(Double currentRate, Double previousRate) {
        if (currentRate == null || previousRate == null) {
            return 0.0;
        }
        double change = currentRate - previousRate;
        return Math.round(change * 10.0) / 10.0; // Làm tròn 1 chữ số thập phân
    }

    private String mapPeriodToLabel(String period) {
        return switch (period.toLowerCase()) {
            case "today" ->
                "Hôm nay";
            case "last_7_days" ->
                "7 ngày qua";
            default ->
                "30 ngày qua";
        };
    }

    @Override
    public List<RevenueTrendDTO> getRevenueTrendData(String period) {
        LocalDate today = LocalDate.now();

        // --- Tuần này ---
        LocalDate startWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endWeek = today.with(DayOfWeek.SUNDAY);
        BigDecimal weekRevenue = getRevenueRange(startWeek.atStartOfDay(), endWeek.atTime(LocalTime.MAX));

        // --- Tuần trước ---
        LocalDate lastWeekStart = startWeek.minusWeeks(1);
        LocalDate lastWeekEnd = endWeek.minusWeeks(1);
        BigDecimal lastWeekRevenue = getRevenueRange(lastWeekStart.atStartOfDay(), lastWeekEnd.atTime(LocalTime.MAX));

        // --- Tháng này ---
        YearMonth ym = YearMonth.from(today);
        BigDecimal monthRevenue = getRevenueRange(ym.atDay(1).atStartOfDay(), ym.atEndOfMonth().atTime(LocalTime.MAX));

        // --- Tháng trước ---
        YearMonth lastMonth = ym.minusMonths(1);
        BigDecimal lastMonthRevenue = getRevenueRange(lastMonth.atDay(1).atStartOfDay(), lastMonth.atEndOfMonth().atTime(LocalTime.MAX));

        return List.of(
                buildTrend("Tuần này", weekRevenue, lastWeekRevenue),
                buildTrend("Tuần trước", lastWeekRevenue, weekRevenue),
                buildTrend("Tháng này", monthRevenue, lastMonthRevenue),
                buildTrend("Tháng trước", lastMonthRevenue, monthRevenue)
        );
    }

    private BigDecimal getRevenueRange(LocalDateTime start, LocalDateTime end) {
        return getRevenueInRange(start, end);
    }

    private RevenueTrendDTO buildTrend(String label, BigDecimal current, BigDecimal previous) {

        double change = 0;
        boolean isUp = false;

        if (previous.compareTo(BigDecimal.ZERO) > 0) {
            change = current.subtract(previous)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(previous, 2, RoundingMode.HALF_UP)
                    .doubleValue();
            isUp = change >= 0;
        }

        return RevenueTrendDTO.builder()
                .label(label)
                .amount(formatCurrency(current))
                .change(Math.abs(change))
                .isUp(isUp)
                .build();
    }

    @Override
    public List<ServiceBreakdownDTO> getServiceBreakdown(String period) {
        return Collections.emptyList(); // Hiện tại trả về rỗng
    }

    @Override
    public AppointmentStatsDTO getAppointmentStats(String period, Integer locationId, String startDate, String endDate) {
        LocalDateTime currentStart;
        LocalDateTime currentEnd;

        // Kiểm tra nếu startDate và endDate được truyền vào
        if (startDate != null && endDate != null) {
            currentStart = LocalDateTime.parse(startDate + "T00:00:00"); // Chuyển startDate thành LocalDateTime
            currentEnd = LocalDateTime.parse(endDate + "T23:59:59"); // Chuyển endDate thành LocalDateTime
        } else {
            // Nếu không có startDate và endDate, sử dụng mặc định theo period
            LocalDateTime[] dates = getPeriodDates(period);
            currentStart = dates[0];
            currentEnd = dates[1];
        }

        // Truy vấn số lượng cuộc hẹn theo khung giờ
        List<Object[]> timeSlotStats = appointmentRepository.countAppointmentsByTimeSlot(currentStart, currentEnd);
        List<TimeSlotStatDTO> timeSlotStatDTO = new ArrayList<>();
        for (Object[] data : timeSlotStats) {
            String timeSlot = (String) data[0];
            long count = (Long) data[1];
            timeSlotStatDTO.add(new TimeSlotStatDTO(timeSlot, count));
        }

        // Truy vấn tổng số cuộc hẹn (Sử dụng BigDecimal)
        BigDecimal totalAppointmentsCurrent = appointmentRepository.countAllAppointments(currentStart, currentEnd);

        // Truy vấn số cuộc hẹn theo từng ngày trong tuần
        List<Object[]> dayData = appointmentRepository.countAppointmentsByDay(currentStart, currentEnd);
        List<DailyStatDTO> dayDataDTO = new ArrayList<>();
        for (Object[] data : dayData) {
            Integer dayIndex = ((Number) data[0]).intValue();  // <-- FIX QUAN TRỌNG
            Long count = ((Number) data[1]).longValue();       // <-- FIX QUAN TRỌNG

            String day = getDayName(dayIndex); // Convert sang tên ngày

            double percent = (count / totalAppointmentsCurrent.doubleValue()) * 100;

            dayDataDTO.add(new DailyStatDTO(day, count, String.format("%.2f", percent) + "%"));
        }

        // Truy vấn tổng số cuộc hẹn đã hoàn thành (Sử dụng BigDecimal)
        BigDecimal completedAppointments = appointmentRepository.countCompletedAppointments(currentStart, currentEnd);

        // Tính tỷ lệ hoàn thành (Sử dụng BigDecimal)
        BigDecimal completionRate = (totalAppointmentsCurrent.compareTo(BigDecimal.ZERO) == 0)
                ? BigDecimal.ZERO
                : completedAppointments.multiply(new BigDecimal(100)).divide(totalAppointmentsCurrent, 2, RoundingMode.HALF_UP);

        // Truy vấn doanh thu từ các cuộc hẹn đã hoàn thành (Sử dụng BigDecimal)
        BigDecimal totalRevenue = appointmentRepository.sumRevenueFromCompletedAppointments(currentStart, currentEnd);

// Truy vấn doanh thu từ các cuộc hẹn đã hoàn thành (Sử dụng BigDecimal)
        BigDecimal totalRevenuePrevious = appointmentRepository.sumRevenueFromCompletedAppointments(currentStart, currentEnd);
        BigDecimal growthRate = BigDecimal.ZERO;

// Kiểm tra nếu totalRevenuePrevious là null, nếu có thì gán giá trị bằng BigDecimal.ZERO
        if (totalRevenuePrevious == null) {
            totalRevenuePrevious = BigDecimal.ZERO;
        }

// Kiểm tra nếu doanh thu kỳ trước khác 0
        if (totalRevenuePrevious.compareTo(BigDecimal.ZERO) > 0) {
            // Tính tỷ lệ tăng trưởng
            growthRate = totalRevenue.subtract(totalRevenuePrevious)
                    .multiply(new BigDecimal(100)) // Nhân với 100 để tính phần trăm
                    .divide(totalRevenuePrevious, 2, RoundingMode.HALF_UP); // Chia và làm tròn 2 chữ số thập phân
        }

        // Truy vấn số cuộc hẹn theo dịch vụ
        List<Object[]> appointmentsByServiceData = appointmentRepository.countAppointmentsByService(currentStart, currentEnd);
        List<ServiceBreakdownDTO> appointmentsByService = new ArrayList<>();

        for (Object[] data : appointmentsByServiceData) {
            String serviceName = (String) data[0];  // Tên dịch vụ

            // Kiểm tra và chuyển kiểu an toàn
            BigDecimal revenueShare;
            if (data[1] instanceof Long) {
                revenueShare = new BigDecimal((Long) data[1]); // Chuyển Long thành BigDecimal
            } else if (data[1] instanceof BigDecimal) {
                revenueShare = (BigDecimal) data[1]; // Nếu đã là BigDecimal thì giữ nguyên
            } else {
                revenueShare = BigDecimal.ZERO; // Giá trị mặc định nếu không phải Long hoặc BigDecimal
            }

            // Kiểm tra và chuyển kiểu cho tổng số cuộc hẹn
            BigDecimal totalAppointmentsForService;
            if (data[2] instanceof Long) {
                totalAppointmentsForService = new BigDecimal((Long) data[2]); // Chuyển Long thành BigDecimal
            } else if (data[2] instanceof BigDecimal) {
                totalAppointmentsForService = (BigDecimal) data[2]; // Nếu là BigDecimal thì giữ nguyên
            } else {
                totalAppointmentsForService = BigDecimal.ZERO; // Giá trị mặc định
            }

            // Thêm vào danh sách
            appointmentsByService.add(ServiceBreakdownDTO.builder()
                    .serviceName(serviceName)
                    .revenueShare(revenueShare)
                    .totalAppointments(totalAppointmentsForService)
                    .build());
        }

        // Trả về thống kê cho tất cả các cuộc hẹn (Giữ BigDecimal cho tất cả các giá trị doanh thu)
        return AppointmentStatsDTO.builder()
                .totalAppointments(totalAppointmentsCurrent) // Giữ BigDecimal
                .completedAppointments(completedAppointments) // Giữ BigDecimal
                .completionRate(completionRate) // Giữ BigDecimal
                .totalRevenue(totalRevenue) // Giữ BigDecimal
                .appointmentsByService(appointmentsByService)
                .growthRate(growthRate)
                .timeSlotStats(timeSlotStatDTO)
                .dailyStats(dayDataDTO)
                .build();
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                return "CN"; // Sunday
            case 2:
                return "T2"; // Monday
            case 3:
                return "T3"; // Tuesday
            case 4:
                return "T4"; // Wednesday
            case 5:
                return "T5"; // Thursday
            case 6:
                return "T6"; // Friday
            case 7:
                return "T7"; // Saturday
            default:
                return "";
        }
    }

    // API để lấy thống kê khách hàng theo các nhóm mới, quay lại, VIP và phân nhóm độ tuổi
    @Override
    public CustomerStatsDTO getCustomerStats(String period, String startDate, String endDate) {
        // Lấy các khoảng thời gian cho thống kê
        LocalDateTime[] periodDates = getPeriodDates(period);

        LocalDateTime currentStart;
        LocalDateTime currentEnd;

        // Kiểm tra nếu startDate và endDate được truyền vào
        if (startDate != null && endDate != null) {
            currentStart = LocalDateTime.parse(startDate + "T00:00:00"); // Chuyển startDate thành LocalDateTime
            currentEnd = LocalDateTime.parse(endDate + "T23:59:59"); // Chuyển endDate thành LocalDateTime
        } else {
            // Nếu không có startDate và endDate, sử dụng mặc định theo period
            currentStart = periodDates[0];
            currentEnd = periodDates[1];
        }

        // Tính số lượng khách hàng mới, quay lại và VIP
        long newCustomers = userRepository.countNewCustomers(currentStart, currentEnd);
        long returningCustomers = userRepository.countReturningCustomers(currentStart, currentEnd);
        long vipCustomers = userRepository.countVIPCustomers(currentStart, currentEnd);

        String startString = currentStart.toLocalDate().toString();
        String endString = currentEnd.toLocalDate().toString();
        // Phân nhóm khách hàng theo độ tuổi
        Map<String, Long> ageGroups = getAgeGroupStats(startString, endString);

        // Trả về kết quả
        return new CustomerStatsDTO(newCustomers, returningCustomers, vipCustomers, ageGroups);
    }

    private Map<String, Long> getAgeGroupStats(String startDate, String endDate) {
        Map<String, Long> ageGroups = new HashMap<>();

        // Lấy ngày hiện tại
        LocalDate currentDate = LocalDate.now();

        // Khai báo các ngày bắt đầu và kết thúc của khoảng thời gian
        LocalDateTime currentStart;
        LocalDateTime currentEnd;

        // Kiểm tra nếu startDate và endDate được truyền vào
        if (startDate != null && endDate != null) {
            // Chuyển startDate và endDate thành LocalDateTime
            currentStart = LocalDateTime.parse(startDate + "T00:00:00"); // Thêm giờ 00:00:00 cho startDate
            currentEnd = LocalDateTime.parse(endDate + "T23:59:59");   // Thêm giờ 23:59:59 cho endDate
        } else {
            // Nếu không có startDate và endDate, sử dụng mặc định theo period
            // Ví dụ, periodDates[0] là ngày bắt đầu, periodDates[1] là ngày kết thúc
            LocalDateTime[] periodDates = getPeriodDates("last_30_days");
            currentStart = periodDates[0];
            currentEnd = periodDates[1];
        }

        // Tính ngày bắt đầu và kết thúc cho từng nhóm độ tuổi
        LocalDate start18_24 = currentDate.minusYears(24); // Để vào nhóm từ 18 đến 24
        LocalDate end18_24 = currentDate.minusYears(18);   // 24 tuổi là giới hạn trên

        LocalDate start25_34 = currentDate.minusYears(34); // Để vào nhóm từ 25 đến 34
        LocalDate end25_34 = currentDate.minusYears(25);   // 34 tuổi là giới hạn trên

        LocalDate start35_50 = currentDate.minusYears(50); // Để vào nhóm từ 35 đến 50
        LocalDate end35_50 = currentDate.minusYears(35);   // 50 tuổi là giới hạn trên

        // Kiểm tra và điều chỉnh lại ngày, nếu cần
        start18_24 = validateDate(start18_24);
        end18_24 = validateDate(end18_24);
        start25_34 = validateDate(start25_34);
        end25_34 = validateDate(end25_34);
        start35_50 = validateDate(start35_50);
        end35_50 = validateDate(end35_50);

        // Truy vấn số lượng khách hàng trong từng nhóm độ tuổi, dựa trên startDate và endDate
        ageGroups.put("18-24", userRepository.countByAgeGroup(start18_24, end18_24, currentStart, currentEnd));
        ageGroups.put("25-34", userRepository.countByAgeGroup(start25_34, end25_34, currentStart, currentEnd));
        ageGroups.put("35-50", userRepository.countByAgeGroup(start35_50, end35_50, currentStart, currentEnd));

        return ageGroups;
    }

    private LocalDate validateDate(LocalDate date) {
        // Kiểm tra xem ngày có hợp lệ không (từ năm 1753 trở đi đối với SQL Server)
        if (date.isBefore(LocalDate.of(1753, 1, 1))) {
            // Nếu không hợp lệ, gán giá trị mặc định (1753-01-01)
            return LocalDate.of(1753, 1, 1);
        }
        return date;  // Nếu hợp lệ, trả lại ngày gốc
    }

    @Override
    public PerformanceStatsDTO getPerformanceStats(String period, String startDate, String endDate) {
        // Tính toán thống kê hiệu suất từ cơ sở dữ liệu
        LocalDateTime[] periodDates = getPeriodDates(period);
        LocalDateTime currentStart = periodDates[0];
        LocalDateTime currentEnd = periodDates[1];

        if (startDate != null && endDate != null) {
            currentStart = LocalDateTime.parse(startDate + "T00:00:00");
            currentEnd = LocalDateTime.parse(endDate + "T23:59:59");
        }

        // Tính toán doanh thu theo sản phẩm và dịch vụ
        BigDecimal serviceRevenue = invoiceRepository.sumRevenueByService(currentStart, currentEnd); // Doanh thu từ dịch vụ
        BigDecimal productRevenue = invoiceRepository.sumRevenueByProduct(currentStart, currentEnd); // Doanh thu từ sản phẩm
        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (serviceRevenue != null) {
            totalRevenue = totalRevenue.add(serviceRevenue);
        }
        if (productRevenue != null) {
            totalRevenue = totalRevenue.add(productRevenue);
        }

        // Đánh giá trung bình
        Double averageRating = reviewRepository.getAverageRating(currentStart, currentEnd);
        BigDecimal averageRatingValue = averageRating != null ? BigDecimal.valueOf(averageRating) : BigDecimal.ZERO;

        // Số lượng khách hàng mới
        long customerCount = userRepository.countNewCustomers(currentStart, currentEnd);

        // Tỷ lệ hoàn thành cuộc hẹn
        BigDecimal totalAppointments = appointmentRepository.countAllAppointments(currentStart, currentEnd);
        BigDecimal completedAppointments = appointmentRepository.countCompletedAppointments(currentStart, currentEnd);
        double completionRate = calculateCompletionRate(totalAppointments, completedAppointments);

        // Tính phần trăm thay đổi cho doanh thu (Giả sử doanh thu kỳ trước là 100M)
        double revenueChangePercent = calculateChangePercent(totalRevenue, BigDecimal.valueOf(100000000)); // Giả sử 100M là doanh thu kỳ trước

        // Doanh thu dịch vụ
        return PerformanceStatsDTO.builder()
                .serviceRevenue(serviceRevenue != null ? serviceRevenue : BigDecimal.ZERO)
                .productRevenue(productRevenue != null ? productRevenue : BigDecimal.ZERO)
                .totalRevenue(totalRevenue)
                .revenueChangePercent(String.format("%.2f%%", revenueChangePercent)) // Tính phần trăm thay đổi
                .averageRating(averageRatingValue)
                .customerCount(BigDecimal.valueOf(customerCount))
                .completionRate(completionRate)
                .build();
    }

    private double calculateCompletionRate(BigDecimal totalAppointments, BigDecimal completedAppointments) {
        if (totalAppointments.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;  // Trả về 0 nếu không có cuộc hẹn nào
        }

        // Tính tỷ lệ hoàn thành (completedAppointments / totalAppointments * 100)
        return completedAppointments.multiply(BigDecimal.valueOf(100))
                .divide(totalAppointments, 2, RoundingMode.HALF_UP).doubleValue();
    }

    // Phương thức tính phần trăm thay đổi giữa giá trị hiện tại và giá trị trước đó
    private double calculateChangePercent(BigDecimal currentRevenue, BigDecimal previousRevenue) {
        // Kiểm tra nếu doanh thu kỳ trước là 0, thì phần trăm thay đổi là 0
        if (previousRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return currentRevenue.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }

        // Tính phần trăm thay đổi
        BigDecimal difference = currentRevenue.subtract(previousRevenue);
        return difference.multiply(BigDecimal.valueOf(100))
                .divide(previousRevenue, 2, RoundingMode.HALF_UP).doubleValue();
    }

    // Hàm định dạng tiền tệ
    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "0";
        }

        // Kiểm tra nếu là triệu (>= 1 triệu)
        if (value.compareTo(new BigDecimal("1000000")) >= 0) {
            return value.divide(new BigDecimal("1000000"), 1, BigDecimal.ROUND_HALF_UP).toString() + "M"; // Triệu
        }

        // Kiểm tra nếu là nghìn (>= 1 nghìn)
        if (value.compareTo(new BigDecimal("1000")) >= 0) {
            return value.divide(new BigDecimal("1000"), 1, BigDecimal.ROUND_HALF_UP).toString() + "K"; // Nghìn
        }

        // Nếu nhỏ hơn 1000, giữ nguyên giá trị
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(value); // Format theo dấu phân cách
    }

    @Override
    public RevenueAnalysisResponse getRevenueAnalysis(String mode, Integer year) {
        LocalDate today = LocalDate.now();
        int analysisYear = (year != null) ? year : today.getYear();

        List<RevenueAnalysisPointDTO> chartData;
        LocalDateTime startRange;
        LocalDateTime endRange;
        switch (mode.toLowerCase()) {
            case "quarter":
                chartData = buildQuarterAnalysis(analysisYear);
                int currentQuarter = (LocalDate.now().getMonthValue() - 1) / 3 + 1;
                int startMonth = (currentQuarter - 1) * 3 + 1;
                int endMonth = startMonth + 2;

                startRange = YearMonth.of(analysisYear, startMonth)
                        .atDay(1).atStartOfDay();

                endRange = YearMonth.of(analysisYear, endMonth)
                        .atEndOfMonth().atTime(LocalTime.MAX);
                break;
            case "year":
                chartData = buildYearAnalysis(analysisYear);
                startRange = LocalDate.of(analysisYear, 1, 1).atStartOfDay();
                endRange = LocalDate.of(analysisYear, 12, 31).atTime(LocalTime.MAX);
                break;
            default:

                chartData = buildMonthAnalysis(analysisYear);

                int currentMonth = LocalDate.now().getMonthValue();
                YearMonth ym = YearMonth.of(analysisYear, currentMonth);

                startRange = ym.atDay(1).atStartOfDay();
                endRange = ym.atEndOfMonth().atTime(LocalTime.MAX);
        }

        // Tính tổng doanh thu
        BigDecimal totalRevenue = chartData.stream()
                .map(RevenueAnalysisPointDTO::getReal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng mục tiêu
        BigDecimal totalTarget = chartData.stream()
                .map(RevenueAnalysisPointDTO::getTarget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // % đạt mục tiêu
        double targetAchieved = totalTarget.compareTo(BigDecimal.ZERO) > 0
                ? totalRevenue.divide(totalTarget, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal(100)).doubleValue()
                : 0;

        // % tăng trưởng (giả lập: so sánh nửa sau & nửa đầu)
        double growthPercent = calculateGrowth(chartData);

        List<RevenueTrendDTO> trends = getRevenueTrendData(mode);

        System.out.println("Start = " + startRange);
        System.out.println("End = " + endRange);

        List<TopServiceDTO> ts = getTopServices(startRange, endRange);
        System.out.println("TOP = " + ts.size());

        return RevenueAnalysisResponse.builder()
                .mode(mode)
                .year(analysisYear)
                .chartData(chartData)
                .totalRevenue(totalRevenue)
                .growthPercent(growthPercent)
                .targetAchieved(targetAchieved)
                .revenueTrends(trends)
                .topServices(getTopServices(startRange, endRange))
                .build();
    }

    // ---------------- MONTH ------------------
    private List<RevenueAnalysisPointDTO> buildMonthAnalysis(int year) {
        List<RevenueAnalysisPointDTO> list = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            YearMonth ym = YearMonth.of(year, month);

            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);

            BigDecimal real = getRevenueInRange(start, end);
            BigDecimal target = real.multiply(new BigDecimal("1.10"));

            list.add(RevenueAnalysisPointDTO.builder()
                    .label("T" + month)
                    .real(real)
                    .target(target)
                    .build());
        }

        return list;
    }

    // ---------------- QUARTER ------------------
    private List<RevenueAnalysisPointDTO> buildQuarterAnalysis(int year) {
        List<RevenueAnalysisPointDTO> list = new ArrayList<>();

        int[][] quarters = {
            {1, 3}, {4, 6}, {7, 9}, {10, 12}
        };

        for (int i = 0; i < quarters.length; i++) {
            YearMonth startYM = YearMonth.of(year, quarters[i][0]);
            YearMonth endYM = YearMonth.of(year, quarters[i][1]);

            LocalDateTime start = startYM.atDay(1).atStartOfDay();
            LocalDateTime end = endYM.atEndOfMonth().atTime(LocalTime.MAX);

            BigDecimal real = getRevenueInRange(start, end);
            BigDecimal target = real.multiply(new BigDecimal("1.10"));

            list.add(RevenueAnalysisPointDTO.builder()
                    .label("Q" + (i + 1))
                    .real(real)
                    .target(target)
                    .build());
        }

        return list;
    }

    // ---------------- YEAR ------------------
    private List<RevenueAnalysisPointDTO> buildYearAnalysis(int year) {
        List<RevenueAnalysisPointDTO> list = new ArrayList<>();

        for (int y = year - 4; y <= year; y++) {
            LocalDateTime start = LocalDate.of(y, 1, 1).atStartOfDay();
            LocalDateTime end = LocalDate.of(y, 12, 31).atTime(LocalTime.MAX);

            BigDecimal real = getRevenueInRange(start, end);
            BigDecimal target = real.multiply(new BigDecimal("1.10"));

            list.add(RevenueAnalysisPointDTO.builder()
                    .label(String.valueOf(y))
                    .real(real)
                    .target(target)
                    .build());
        }

        return list;
    }

    // ------------ Helper ---------------
    private BigDecimal getRevenueInRange(LocalDateTime start, LocalDateTime end) {
        BigDecimal result = invoiceRepository.sumAllPaidInvoices(start, end);
        return result != null ? result : BigDecimal.ZERO;
    }

    private double calculateGrowth(List<RevenueAnalysisPointDTO> data) {
        if (data.size() < 2) {
            return 0;
        }

        int half = data.size() / 2;

        BigDecimal firstHalf = data.subList(0, half).stream()
                .map(RevenueAnalysisPointDTO::getReal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal secondHalf = data.subList(half, data.size()).stream()
                .map(RevenueAnalysisPointDTO::getReal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (firstHalf.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        return secondHalf.subtract(firstHalf)
                .divide(firstHalf, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)).doubleValue();
    }

    @Override
    public List<TopServiceDTO> getTopServices(LocalDateTime start, LocalDateTime end) {

        long days = Duration.between(start, end).toDays() + 1;

        LocalDateTime prevStart = start.minusDays(days);
        LocalDateTime prevEnd = end.minusDays(days);

        List<Object[]> current = appointmentRepository.findTopServices(start, end);
        List<Object[]> previous = appointmentRepository.findTopServices(prevStart, prevEnd);

        Map<String, BigDecimal> prevRevenueMap = new HashMap<>();

        for (Object[] row : previous) {
            String name = (String) row[0];
            BigDecimal revenue = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            prevRevenueMap.put(name, revenue);
        }

        List<TopServiceDTO> result = new ArrayList<>();

        for (Object[] row : current) {
            String name = (String) row[0];
            Long appointments = ((Number) row[1]).longValue();
            BigDecimal revenue = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;

            BigDecimal prevRev = prevRevenueMap.getOrDefault(name, BigDecimal.ZERO);

            double change = calcPercentChange(revenue, prevRev);

            result.add(
                    TopServiceDTO.builder()
                            .name(name)
                            .appointments(appointments)
                            .revenue(revenue.longValue())
                            .change(Math.abs(change))
                            .up(change >= 0)
                            .build()
            );
        }

        return result;
    }

    private double calcPercentChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? 0 : 100;
        }
        return current.subtract(previous)
                .multiply(new BigDecimal(100))
                .divide(previous, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public Map<String, CategoryStatDTO> getCategorySummary() {

        Map<String, CategoryStatDTO> result = new HashMap<>();

        result.put("customer", buildCustomerCategory());
        result.put("revenue", buildRevenueCategory());
        result.put("appointment", buildAppointmentCategory());
        result.put("product", buildProductCategory());

        return result;
    }

    private MetricDTO metric(double current, double previous) {
        double change = (previous == 0)
                ? (current > 0 ? 100 : 0)
                : ((current - previous) / previous) * 100;

        return MetricDTO.builder()
                .current(current)
                .previous(previous)
                .changePercent(Math.round(change * 10.0) / 10.0)
                .up(change >= 0)
                .build();
    }

    private CategoryStatDTO buildCustomerCategory() {

        double todayCurrent = userRepository.countNewCustomers(LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
        double todayPrev = userRepository.countNewCustomers(LocalDate.now().minusDays(1).atStartOfDay(),
                LocalDate.now().minusDays(1).atTime(LocalTime.MAX));

        double weekCurrent = userRepository.countNewCustomers(LocalDate.now().minusDays(6).atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
        double weekPrev = userRepository.countNewCustomers(LocalDate.now().minusDays(13).atStartOfDay(), LocalDate.now().minusDays(7).atTime(LocalTime.MAX));

        double monthCurrent = userRepository.countNewCustomers(LocalDate.now().minusDays(29).atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
        double monthPrev = userRepository.countNewCustomers(LocalDate.now().minusDays(59).atStartOfDay(), LocalDate.now().minusDays(30).atTime(LocalTime.MAX));

        double quarterCurrent = userRepository.countNewCustomers(LocalDate.now().minusMonths(3).atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
        double quarterPrev = userRepository.countNewCustomers(LocalDate.now().minusMonths(6).atStartOfDay(), LocalDate.now().minusMonths(3).atTime(LocalTime.MAX));

        return CategoryStatDTO.builder()
                .today(metric(todayCurrent, todayPrev))
                .week(metric(weekCurrent, weekPrev))
                .month(metric(monthCurrent, monthPrev))
                .quarter(metric(quarterCurrent, quarterPrev))
                .build();
    }

    private CategoryStatDTO buildRevenueCategory() {

        double today = getRevenueInRange(LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX)).doubleValue();
        double yesterday = getRevenueInRange(LocalDate.now().minusDays(1).atStartOfDay(), LocalDate.now().minusDays(1).atTime(LocalTime.MAX)).doubleValue();

        double week = getRevenueInRange(LocalDate.now().minusDays(6).atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX)).doubleValue();
        double weekPrev = getRevenueInRange(LocalDate.now().minusDays(13).atStartOfDay(), LocalDate.now().minusDays(7).atTime(LocalTime.MAX)).doubleValue();

        double month = getRevenueInRange(LocalDate.now().minusDays(29).atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX)).doubleValue();
        double monthPrev = getRevenueInRange(LocalDate.now().minusDays(59).atStartOfDay(), LocalDate.now().minusDays(30).atTime(LocalTime.MAX)).doubleValue();

        double quarter = getRevenueInRange(LocalDate.now().minusMonths(3).atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX)).doubleValue();
        double quarterPrev = getRevenueInRange(LocalDate.now().minusMonths(6).atStartOfDay(), LocalDate.now().minusMonths(3).atTime(LocalTime.MAX)).doubleValue();

        return CategoryStatDTO.builder()
                .today(metric(today, yesterday))
                .week(metric(week, weekPrev))
                .month(metric(month, monthPrev))
                .quarter(metric(quarter, quarterPrev))
                .build();
    }

    private CategoryStatDTO buildAppointmentCategory() {

        double today = appointmentRepository.countAllAppointments(LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX)).doubleValue();
        double yesterday = appointmentRepository.countAllAppointments(LocalDate.now().minusDays(1).atStartOfDay(),
                LocalDate.now().minusDays(1).atTime(LocalTime.MAX)).doubleValue();

        double week = appointmentRepository.countAllAppointments(LocalDate.now().minusDays(6).atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)).doubleValue();
        double weekPrev = appointmentRepository.countAllAppointments(LocalDate.now().minusDays(13).atStartOfDay(),
                LocalDate.now().minusDays(7).atTime(LocalTime.MAX)).doubleValue();

        double month = appointmentRepository.countAllAppointments(LocalDate.now().minusDays(29).atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)).doubleValue();
        double monthPrev = appointmentRepository.countAllAppointments(LocalDate.now().minusDays(59).atStartOfDay(),
                LocalDate.now().minusDays(30).atTime(LocalTime.MAX)).doubleValue();

        double quarter = appointmentRepository.countAllAppointments(LocalDate.now().minusMonths(3).atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)).doubleValue();
        double quarterPrev = appointmentRepository.countAllAppointments(LocalDate.now().minusMonths(6).atStartOfDay(),
                LocalDate.now().minusMonths(3).atTime(LocalTime.MAX)).doubleValue();

        return CategoryStatDTO.builder()
                .today(metric(today, yesterday))
                .week(metric(week, weekPrev))
                .month(metric(month, monthPrev))
                .quarter(metric(quarter, quarterPrev))
                .build();
    }

    private CategoryStatDTO buildProductCategory() {

        BigDecimal todayBD = invoiceRepository.sumRevenueByProduct(
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        );
        double today = todayBD != null ? todayBD.doubleValue() : 0.0;

        BigDecimal yesterdayBD = invoiceRepository.sumRevenueByProduct(
                LocalDate.now().minusDays(1).atStartOfDay(),
                LocalDate.now().minusDays(1).atTime(LocalTime.MAX)
        );
        double yesterday = yesterdayBD != null ? yesterdayBD.doubleValue() : 0.0;

        BigDecimal weekBD = invoiceRepository.sumRevenueByProduct(
                LocalDate.now().minusDays(6).atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        );
        double week = weekBD != null ? weekBD.doubleValue() : 0.0;

        BigDecimal weekPrevBD = invoiceRepository.sumRevenueByProduct(
                LocalDate.now().minusDays(13).atStartOfDay(),
                LocalDate.now().minusDays(7).atTime(LocalTime.MAX)
        );
        double weekPrev = weekPrevBD != null ? weekPrevBD.doubleValue() : 0.0;

        BigDecimal monthBD = invoiceRepository.sumRevenueByProduct(
                LocalDate.now().minusDays(29).atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        );
        double month = monthBD != null ? monthBD.doubleValue() : 0.0;

        BigDecimal monthPrevBD = invoiceRepository.sumRevenueByProduct(
                LocalDate.now().minusDays(59).atStartOfDay(),
                LocalDate.now().minusDays(30).atTime(LocalTime.MAX)
        );
        double monthPrev = monthPrevBD != null ? monthPrevBD.doubleValue() : 0.0;

        BigDecimal quarterBD = invoiceRepository.sumRevenueByProduct(
                LocalDate.now().minusMonths(3).atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        );
        double quarter = quarterBD != null ? quarterBD.doubleValue() : 0.0;

        BigDecimal quarterPrevBD = invoiceRepository.sumRevenueByProduct(
                LocalDate.now().minusMonths(6).atStartOfDay(),
                LocalDate.now().minusMonths(3).atTime(LocalTime.MAX)
        );
        double quarterPrev = quarterPrevBD != null ? quarterPrevBD.doubleValue() : 0.0;

        return CategoryStatDTO.builder()
                .today(metric(today, yesterday))
                .week(metric(week, weekPrev))
                .month(metric(month, monthPrev))
                .quarter(metric(quarter, quarterPrev))
                .build();
    }

}
