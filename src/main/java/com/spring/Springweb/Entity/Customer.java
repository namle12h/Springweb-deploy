package com.spring.Springweb.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CUSTOMER")
@Getter
@Setter
public class Customer extends User {


    @Column(name = "total_spent")
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "rank_level")
    private String rankLevel = "NEWBIE";

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    @Column(name = "notes", length = 500)
    private String notes;
}
