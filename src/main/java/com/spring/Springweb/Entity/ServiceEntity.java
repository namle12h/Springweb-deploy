package com.spring.Springweb.Entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Service")
@NamedQueries({
    @NamedQuery(name = "Service.findAll", query = "SELECT s FROM ServiceEntity s"),
    @NamedQuery(name = "Service.findById", query = "SELECT s FROM ServiceEntity s WHERE s.id = :id"),
    @NamedQuery(name = "Service.findByName", query = "SELECT s FROM ServiceEntity s WHERE s.name = :name"),
    @NamedQuery(name = "Service.findByPrice", query = "SELECT s FROM ServiceEntity s WHERE s.price = :price"),
    @NamedQuery(name = "Service.findByDurationMin", query = "SELECT s FROM ServiceEntity s WHERE s.durationMin = :durationMin"),
    @NamedQuery(name = "Service.findByActive", query = "SELECT s FROM ServiceEntity s WHERE s.active = :active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false, updatable = false)
    private Integer id;

    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "Name", nullable = false, length = 200)
    private String name;

    @NotNull
    @Column(name = "Price", nullable = false)
    private BigDecimal price;

    @NotNull
    @Column(name = "DurationMin", nullable = false)
    private int durationMin;
    
   

    @NotNull
    @Column(name = "Active", nullable = false)
    private Boolean active; // ✅ đổi sang Boolean để Lombok sinh getActive()

    @Column(name = "Description", length = 1000)
    private String description;

    @Column(name = "ImageUrl", length = 255)
    private String imageUrl;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "serviceId")
    @JsonIgnore
    private Collection<ServiceSection> serviceSectionCollection;

    @OneToMany(mappedBy = "service")
    @JsonIgnore
    private Collection<InvoiceItem> invoiceItemCollection;

//    @OneToMany(mappedBy = "service")
//    private Collection<Review> reviewCollection;

    @OneToMany(mappedBy = "service")
    @JsonIgnore
    private Collection<Appointment> appointmentCollection;

    @Override
    public String toString() {
        return "ServiceEntity [id=" + id + ", name=" + name + "]";
    }
}
