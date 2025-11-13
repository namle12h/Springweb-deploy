/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Repository;

import com.spring.Springweb.Entity.Customer;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // Có thể thêm custom query nếu cần
    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByEmail(String phone);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    // Đếm số lượng khách hàng mới
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countNewCustomers(LocalDateTime startDate);

    // Đếm số lượng khách hàng quay lại (đã có ít nhất 1 cuộc hẹn)
    @Query("SELECT COUNT(u) FROM User u WHERE u.appointmentAsCustomer IS NOT EMPTY")
    long countReturningCustomers();

    // Đếm số lượng khách hàng VIP
    @Query("SELECT COUNT(u) FROM User u WHERE u.loyaltyPoints >= 100")
    long countVIPCustomers();

}
