/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Repository;

import com.spring.Springweb.Entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author ADMIN
 */
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long>{
    
}
