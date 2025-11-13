/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author ADMIN
 */
@Entity
@Getter
@Setter
@Table(name = "ServiceSectionItem")
public class ServiceSectionItem implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Integer id;
    @Size(max = 200)
    @Column(name = "Title")
    private String title;
    @Size(max = 2147483647)
    @Column(name = "Description")
    private String description;
    @Column(columnDefinition = "NVARCHAR(MAX)",name="ExtraData")
    private String extraData; // JSON data cho benefit, lưu {"before": "...", "after": "...", "items": [...]}
    private Integer times;
    @Size(max = 500)
    @Column(name = "ImageUrl")
    private String imageUrl;
    @Column(name = "ExtraOrder")
    private Integer extraOrder;
    @JoinColumn(name = "SectionId", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private ServiceSection sectionId;

    public ServiceSectionItem() {
    }

    public ServiceSectionItem(Integer id) {
        this.id = id;
    }

}
