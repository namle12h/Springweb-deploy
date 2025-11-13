package com.spring.Springweb.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.spring.Springweb.Entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailOrUsername(String email, String username);

    Optional<User> findByPhone(String phone);

    @Query("SELECT COUNT(c) FROM User c WHERE c.createdAt BETWEEN :start AND :end")
    Long countNewCustomers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Đếm số lượng khách hàng quay lại
    @Query("SELECT COUNT(u) FROM User u WHERE u.appointmentAsCustomer IS NOT EMPTY  AND u.createdAt BETWEEN :start AND :end")
    long countReturningCustomers(@Param("start") LocalDateTime start,@Param("end") LocalDateTime end);

    // Đếm số lượng khách hàng VIP
    @Query("SELECT COUNT(u) FROM User u WHERE u.loyaltyPoints >= 100 AND u.createdAt BETWEEN :start AND :end")
    long countVIPCustomers(@Param("start") LocalDateTime start,@Param("end") LocalDateTime end);

    // Đếm số lượng khách hàng trong từng nhóm độ tuổi
    @Query("SELECT COUNT(u) FROM User u  WHERE   u.dob BETWEEN :start AND :end AND u.role = 'CUSTOMER' AND u.createdAt BETWEEN :startTime AND :endTime")
    long countByAgeGroup(@Param("start") LocalDate startDate, @Param("end") LocalDate endDate ,@Param("startTime") LocalDateTime start,@Param("endTime") LocalDateTime end);

}
