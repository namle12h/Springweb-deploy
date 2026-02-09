/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Repository;


import com.spring.Springweb.Entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    List<ProductImage> findByProduct_IdOrderByIsPrimaryDescSortOrderAsc(Integer productId);
    
     
     List<ProductImage> findByProductIdOrderBySortOrderAsc(Integer productId);
}
