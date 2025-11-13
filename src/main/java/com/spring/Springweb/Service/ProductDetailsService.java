/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.ProductDetailsDTO;
import com.spring.Springweb.Entity.ProductDetails;

/**
 *
 * @author ADMIN
 */
public interface ProductDetailsService {

    public ProductDetailsDTO getDetailsByProductId(Integer productId);

    public ProductDetailsDTO saveOrUpdate(Integer productId, ProductDetailsDTO dto);

    public void deleteByProductId(Integer productId);

}
