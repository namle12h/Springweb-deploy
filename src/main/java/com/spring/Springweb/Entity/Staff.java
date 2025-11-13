package com.spring.Springweb.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("STAFF")
@Getter
@Setter
public class Staff extends User {

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "base_salary")
    private BigDecimal baseSalary = BigDecimal.ZERO;

    @Column(name = "bonus")
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(name = "commission_rate")
    private BigDecimal commissionRate = BigDecimal.ZERO;

    @Column(name = "staff_rank")
    private String staffRank = "BRONZE";

    @Column(name = "total_revenue")
    private BigDecimal totalRevenue = BigDecimal.ZERO;
}
