package com.spring.Springweb.Entity;

import java.time.LocalDateTime;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "Appointment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"invoiceCollection", "customer", "staff", "room"}) // tránh vòng lặp khi in
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "Id")
    private Integer id;

    @NotNull
    @Column(name = "StartAt", nullable = false)
    private LocalDateTime startAt;

    @NotNull
    @Column(name = "EndAt", nullable = false)
    private LocalDateTime endAt;

    @NotNull
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    // Khách hàng
    @ManyToOne
    @JoinColumn(name = "CustomerId", nullable = false)
    @JsonIgnore
    private Customer customer;

    // Nhân viên phụ trách
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "StaffId")
    private Staff staff;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @Column(name = "Notes", length = 255)
    private String notes;

    private String contactName;   // snapshot contact
    private String contactEmail;
    private String contactPhone;

    @Transient
    @JsonProperty("staffId")
    private Integer staffId;  // staffId tạm cho JSON
    @Transient
    @JsonProperty("roomId")
    private Integer roomId;   // roomId tạm cho JSON
    @JsonProperty("serviceId")
    @Transient
    private Integer serviceId;

     private LocalDateTime updatedAt;
    // Liên kết tới phòng
    @ManyToOne
    @JoinColumn(name = "RoomId")
    @JsonIgnore
    private Room room;

    // Liên kết tới phòng
    @ManyToOne
    @JoinColumn(name = "serviceId")
    @JsonIgnore
    private ServiceEntity service;

    @OneToOne(mappedBy = "appointment")
    @JsonIgnore
    private Invoice invoice;

}
