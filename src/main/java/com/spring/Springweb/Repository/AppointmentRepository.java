/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spring.Springweb.Entity.Appointment;
import com.spring.Springweb.Entity.Customer;
import com.spring.Springweb.Entity.ServiceEntity;
import com.spring.Springweb.Entity.User;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    // Tìm theo customerId
    List<Appointment> findByCustomer_Id(Integer customerId);

    // Tìm theo staffId
    List<Appointment> findByStaff_Id(Integer staffId);

    // Tìm theo status
    List<Appointment> findByStatus(String status);

    List<Appointment> findByCustomer(User customer);

    List<Appointment> findByCustomerOrderByCreatedAtDesc(User customer);

    // Tìm số lượng lịch hẹn theo khách + dịch vụ trong khoảng thời gian
    long countByCustomerAndServiceAndStartAtBetween(
            Customer customer,
            ServiceEntity service,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.startAt BETWEEN :start AND :end")
    BigDecimal countAllAppointments(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'PAID' AND a.startAt BETWEEN :start AND :end")
    BigDecimal countCompletedAppointments(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(s.price) FROM Appointment a JOIN a.service s WHERE a.status = 'PAID' AND a.startAt BETWEEN :start AND :end")
    BigDecimal sumRevenueFromCompletedAppointments(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a.service.name, SUM(s.price) AS revenueShare, COUNT(a) AS totalAppointments FROM Appointment a JOIN a.service s WHERE a.startAt BETWEEN :start AND :end GROUP BY a.service.name")
    List<Object[]> countAppointmentsByService(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Truy vấn số lượng cuộc hẹn theo từng khung giờ trong khoảng thời gian
//     @Query("SELECT CONCAT(CAST(FORMAT(a.startAt, 'HH:mm') AS string), ' - ', CAST(FORMAT(a.startAt, 'HH:mm') AS string)) AS timeSlot, COUNT(a) "
//             + "FROM Appointment a WHERE a.startAt BETWEEN :startDate AND :endDate "
//             + "GROUP BY CAST(FORMAT(a.startAt, 'HH:mm') AS string)")
//     List<Object[]> countAppointmentsByTimeSlot(LocalDateTime startDate, LocalDateTime endDate);
@Query(value = """
    SELECT 
        DATE_FORMAT(a.startAt, '%H:%i') AS timeSlot,
        COUNT(a.id) AS total
    FROM Appointment a
    WHERE a.startAt BETWEEN :startDate AND :endDate
    GROUP BY timeSlot
    ORDER BY timeSlot
""", nativeQuery = true)
List<Object[]> countAppointmentsByTimeSlot(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
);


    // Truy vấn tổng số cuộc hẹn cho mỗi ngày trong tuần
//     @Query(value = "SELECT DATEPART(weekday, a.startAt) AS dayOfWeek, COUNT(a.id) "
//             + "FROM Appointment a WHERE a.startAt BETWEEN :startDate AND :endDate "
//             + "GROUP BY DATEPART(weekday, a.startAt)", nativeQuery = true)
//     List<Object[]> countAppointmentsByDay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
@Query(value = """
    SELECT 
        WEEKDAY(a.startAt) + 1 AS dayOfWeek,
        COUNT(a.Id) AS total
    FROM Appointment a
    WHERE a.startAt BETWEEN :startDate AND :endDate
    GROUP BY dayOfWeek
    ORDER BY dayOfWeek
""", nativeQuery = true)
List<Object[]> countAppointmentsByDay(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
);


    @Query("""
    SELECT 
        s.name AS serviceName,
        COUNT(a.id) AS totalAppointments,
        SUM(i.total) AS totalRevenue
    FROM Appointment a
    JOIN a.service s
    JOIN a.invoice i
    WHERE i.status = 'PAID'
      AND a.createdAt BETWEEN :start AND :end
    GROUP BY s.name
    ORDER BY totalRevenue DESC
       LIMIT 5
""")
    List<Object[]> findTopServices(LocalDateTime start, LocalDateTime end);

}
