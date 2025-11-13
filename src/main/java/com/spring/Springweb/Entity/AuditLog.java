package com.spring.Springweb.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "AuditLog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entity;       // Appointment, Invoice, Product...
    private Long entityId;       // ID của record
    private String action;       // CREATE | UPDATE | DELETE | CANCEL
    private String field;        // nếu muốn log chi tiết field thay đổi
    private String oldValue;
    private String newValue;
    private String status; // success / failed
    private String role;
    private Long performedBy;    // users.id
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime performedAt;
    private String performedByName; // Tên người thao tác
}
