package com.spring.Springweb.Entity;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "Invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"payment", "appointment", "customer", "invoice"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Invoice implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "Id")
    private Integer id;

    // 🧾 Tổng tiền cuối cùng sau thuế & giảm giá
    @NotNull
    @Column(name = "Total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    // 💰 Trạng thái thanh toán (PAID / UNPAID / CANCELED)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    // 🕓 Ngày tạo hóa đơn
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    // 💵 Quan hệ 1-1 với bảng Payment
    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL)
    private Payment payment;

    // 📅 Quan hệ với bảng Appointment
    @OneToOne
    @JoinColumn(name = "AppointmentId", referencedColumnName = "Id", nullable = true)
    private Appointment appointment;

    // 👤 Quan hệ với bảng User (Customer)
    @ManyToOne(optional = false)
    @JoinColumn(name = "CustomerId", referencedColumnName = "Id")
    private User customer;

    // 🧩 Quan hệ với bảng InvoiceItem (danh sách dịch vụ/sản phẩm)
//    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
//    private Collection<InvoiceItem> invoiceItemCollection;
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvoiceItem> items;

    // 🧮 (Tuỳ chọn) Tổng trước thuế
    @Column(name = "SubTotal", precision = 10, scale = 2)
    private BigDecimal subTotal;

    // 🧾 (Tuỳ chọn) Thuế VAT %
    @Column(name = "VAT", precision = 5, scale = 2)
    private BigDecimal vat;

    // 💸 (Tuỳ chọn) Số tiền giảm giá
    @Column(name = "DiscountAmount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    // 💳 Phương thức thanh toán (Tiền mặt, VNPay, QR, MoMo, ...)
    @Column(name = "PaymentMethod", length = 50)
    private String paymentMethod;

    // 💵 Số tiền khách đã đưa
    @Column(name = "AmountPaid", precision = 15, scale = 2)
    private BigDecimal amountPaid;

    // 💰 Tiền thừa (hoặc còn thiếu nếu âm)
    @Column(name = "ChangeAmount", precision = 15, scale = 2)
    private BigDecimal changeAmount;

    // 📝 Ghi chú đặc biệt
    @Column(name = "Notes", length = 255)
    private String notes;

    // 🔖 Mã giao dịch từ cổng thanh toán (VNPay, MoMo...)
    @Column(name = "TransactionId", length = 100)
    private String transactionId;

    // 🕒 Ngày cập nhật trạng thái thanh toán
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "TxnRef", length = 50)
    private String txnRef;

    @Column(name = "ExpiredAt")
    private LocalDateTime expiredAt;

// ✅ THÔNG TIN GIAO HÀNG
    @Column(name = "ReceiverName", length = 100)
    private String receiverName;

    @Column(name = "ReceiverPhone", length = 20)
    private String receiverPhone;

    @Column(name = "AddressDetail", length = 255)
    private String addressDetail;

    @Column(name = "CityName", length = 100)
    private String cityName;

    @Column(name = "DistrictName", length = 100)
    private String districtName;

    @Column(name = "CommuneName", length = 100)
    private String communeName;

}
