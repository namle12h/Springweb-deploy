/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spring.Springweb.Entity.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    List<Invoice> findByCustomer_Id(Integer customerId);

    List<Invoice> findByAppointment_Staff_Id(Integer staffId);

    Optional<Invoice> findByTxnRef(String txnRef);

    List<Invoice> findByStatusAndExpiredAtBefore(String status, LocalDateTime time);

    @Query("SELECT SUM(i.total) FROM Invoice i WHERE i.status = 'PAID' AND i.createdAt  BETWEEN :start AND :end AND i.appointment IS NOT NULL")
    BigDecimal sumPaidInvoices(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(i.total) FROM Invoice i  WHERE  i.createdAt BETWEEN :start AND :end AND i.status = 'PAID' AND i.appointment IS NOT NULL")
    BigDecimal sumRevenueByService(LocalDateTime start, LocalDateTime end);

    // Truy vấn doanh thu theo sản phẩm (ví dụ: bán sản phẩm skincare)
    @Query("SELECT SUM(i.total) FROM Invoice i WHERE  i.createdAt BETWEEN :start AND :end AND i.status = 'PAID' AND i.appointment IS NULL")
    BigDecimal sumRevenueByProduct(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(i.total) FROM Invoice i WHERE i.status = 'PAID' AND i.createdAt BETWEEN :start AND :end")
    BigDecimal sumAllPaidInvoices(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

  

}
