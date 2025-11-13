package com.spring.Springweb.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "ServiceSection")
@NamedQueries({
    @NamedQuery(name = "ServiceSection.findAll", query = "SELECT s FROM ServiceSection s"),
    @NamedQuery(name = "ServiceSection.findById", query = "SELECT s FROM ServiceSection s WHERE s.id = :id"),
    @NamedQuery(name = "ServiceSection.findByTitle", query = "SELECT s FROM ServiceSection s WHERE s.title = :title"),
    @NamedQuery(name = "ServiceSection.findBySortOrder", query = "SELECT s FROM ServiceSection s WHERE s.sortOrder = :sortOrder")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceSection implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Integer id;

    @Size(max = 200)
    @Column(name = "Title")
    private String title;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "SortOrder")
    private int sortOrder;

    @ManyToOne(optional = false)
    @JoinColumn(name = "SectionType", referencedColumnName = "Code")
    private SectionType sectionType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ServiceId", referencedColumnName = "Id")
    private ServiceEntity serviceId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sectionId")
    private Collection<ServiceSectionItem> serviceSectionItemCollection;

    @Override
    public String toString() {
        return "ServiceSection [id=" + id + ", title=" + title + "]";
    }
}
