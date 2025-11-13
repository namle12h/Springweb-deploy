package com.spring.Springweb.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "Room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "Type", nullable = false, length = 50)
    private String type;

    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @NotNull
    @Column(name = "Capacity", nullable = false)
    private int capacity;

    @Column(name = "Notes", length = 200)
    private String notes;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CreatedAt", nullable = false)
    private Date createdAt;

    // Quan hệ 1-nhiều với Appointment
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Appointment> appointments;
}
