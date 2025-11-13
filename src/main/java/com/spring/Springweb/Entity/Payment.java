/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author ADMIN
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Payment")
public class Payment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "Method")
    private String method;

    @Column(name = "Amount")
    private BigDecimal amount;

    @NotNull
    @Column(name = "PaidAt")
    private LocalDateTime paidAt = LocalDateTime.now();

    @OneToOne(optional = false)
    @JoinColumn(name = "InvoiceId", referencedColumnName = "Id")
    private Invoice invoice;   // ✅ đổi từ invoiceId -> invoice

    // Getters & Setters ...
}
