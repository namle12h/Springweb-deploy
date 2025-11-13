package com.spring.Springweb.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "InvoiceItem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @NotNull
    @Column(name = "Qty", nullable = false)
    private int qty;

    @NotNull
    @Column(name = "UnitPrice", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "LineTotal", insertable = false, updatable = false)
    private BigDecimal lineTotal;

    

    @ManyToOne(optional = false)
    @JoinColumn(name = "InvoiceId", referencedColumnName = "Id", nullable = false)
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "ProductId", referencedColumnName = "Id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "ServiceId", referencedColumnName = "Id")
    private ServiceEntity service;

}
