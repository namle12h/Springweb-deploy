package com.spring.Springweb.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "Review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"customer"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Review implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "Id")
    private Integer id;

    @NotNull
    @Column(name = "Rating", nullable = false)
    private int rating;

    @Size(max = 255)
    @Column(name = "Comment")
    private String comment;

    @Size(max = 255)
    @Column(name = "ImageUrl")
    private String imageUrl;

    @Column(name = "Reply")
    private String reply;

    @Column(name = "IsApproved")
    private Boolean isApproved = false;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UpdatedAt")
    private Date updatedAt;

    // 🔹 Xác định loại đánh giá: Product hoặc Service
    @NotBlank
    @Column(name = "ReviewType", nullable = false, length = 20)
    private String reviewType;

    // 🔹 ID thực thể được đánh giá (sản phẩm hoặc dịch vụ)
    @NotNull
    @Column(name = "RefId", nullable = false)
    private Integer refId;

    // 🔹 Liên kết Customer/User (bắt buộc)
    @ManyToOne(optional = false)
    @JoinColumn(name = "CustomerId", referencedColumnName = "Id", nullable = false)
    private User customer;

    @Transient
    private Integer customerId;

}
