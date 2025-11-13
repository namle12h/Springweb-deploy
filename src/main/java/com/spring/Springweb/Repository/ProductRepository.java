 ///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.spring.Springweb.Repository;
//
//import java.util.List;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import com.spring.Springweb.Entity.Product;
//
///**
// *
// * @author ADMIN
// */
//public interface ProductRepository extends JpaRepository<Product, Integer> {
//
//    @Query("""
//    SELECT DISTINCT p
//    FROM Product p
//    LEFT JOIN FETCH p.details
//    LEFT JOIN FETCH p.images
//    LEFT JOIN FETCH User u1 ON p.createdBy = u1.id
//    LEFT JOIN FETCH User u2 ON p.updatedBy = u2.id
//""")
//    List<Product> findAllWithDetailsAndUsers();
//
//}


package com.spring.Springweb.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spring.Springweb.Entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // ✅ Bước 1: Lấy danh sách ID theo phân trang thật
    @Query("SELECT p.id FROM Product p ")
    Page<Integer> findProductIds(Pageable pageable);

    // ✅ Bước 2: Lấy sản phẩm đầy đủ (fetch join để không lazy)
    @EntityGraph(attributePaths = {"details", "images", "createdBy", "updatedBy"})
    @Query("SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id DESC")
    List<Product> findAllWithDetailsAndUsersByIds(@Param("ids") List<Integer> ids);

}
