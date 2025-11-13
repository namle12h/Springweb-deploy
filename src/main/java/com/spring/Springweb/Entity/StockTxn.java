package com.spring.Springweb.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "StockTxn")
@NamedQueries({
        @NamedQuery(name = "StockTxn.findAll", query = "SELECT s FROM StockTxn s"),
        @NamedQuery(name = "StockTxn.findById", query = "SELECT s FROM StockTxn s WHERE s.id = :id"),
        @NamedQuery(name = "StockTxn.findByTxnType", query = "SELECT s FROM StockTxn s WHERE s.txnType = :txnType"),
        @NamedQuery(name = "StockTxn.findByQty", query = "SELECT s FROM StockTxn s WHERE s.qty = :qty"),
        @NamedQuery(name = "StockTxn.findByCreatedAt", query = "SELECT s FROM StockTxn s WHERE s.createdAt = :createdAt"),
        @NamedQuery(name = "StockTxn.findByNotes", query = "SELECT s FROM StockTxn s WHERE s.notes = :notes")
})
@Data   // ✅ Tự sinh getter/setter, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder // ✅ Cho phép tạo object kiểu builder
public class StockTxn implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "TxnType", nullable = false)
    private String txnType;

    @Column(name = "Qty")
    private BigDecimal qty;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CreatedAt", nullable = false)
    private Date createdAt;

    @Size(max = 200)
    @Column(name = "Notes")
    private String notes;

    // ✅ Đổi productId -> product cho đúng JPA
    @ManyToOne(optional = false)
    @JoinColumn(name = "ProductId", referencedColumnName = "Id")
    private Product product;
}
