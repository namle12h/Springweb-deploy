package com.spring.Springweb.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {
    "appointmentAsCustomer",
    "appointmentAsStaff",
    "invoices",
    "reviews"
})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @NotNull
    @Size(min = 3, max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotNull
    @Column(name = "password_hash", nullable = false)
    @JsonIgnore
    private String passwordHash;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 10)
    private String gender;

    @Column(length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "address")
    private String address;

    @Column(name = "role", insertable = false, updatable = false)
    private String role;

    private LocalDate dob;

    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private java.util.List<Appointment> appointmentAsCustomer;

    @OneToMany(mappedBy = "staff")
    @JsonIgnore
    private java.util.List<Appointment> appointmentAsStaff;

    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private java.util.List<Invoice> invoices;

    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private java.util.List<Review> reviews;

    public enum Role {
        ADMIN, STAFF, CUSTOMER
    }
}
