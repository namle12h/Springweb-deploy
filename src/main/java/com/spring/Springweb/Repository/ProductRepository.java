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
//    @EntityGraph(attributePaths = {"details", "images", "createdBy", "updatedBy"})
//    @Query("SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id DESC")
//    List<Product> findAllWithDetailsAndUsersByIds(@Param("ids") List<Integer> ids);
//    
    @Query("""
        SELECT p.id
        FROM Product p
        WHERE p.category.id = :categoryId
        ORDER BY p.id DESC
    """)
    Page<Integer> findProductIdsByCategory(
            @Param("categoryId") Integer categoryId,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT p
        FROM Product p
        LEFT JOIN FETCH p.details
        LEFT JOIN FETCH p.images
        WHERE p.id IN :ids
    """)
    List<Product> findAllWithDetailsAndUsersByIds(
            @Param("ids") List<Integer> ids
    );

    @Query("""
SELECT p FROM Product p
WHERE p.category.id = :categoryId
AND p.id <> :productId
AND p.active = true
ORDER BY p.discountPercent DESC, p.id DESC
""")
    List<Product> findRelatedProducts(
            @Param("categoryId") Integer categoryId,
            @Param("productId") Integer productId,
            Pageable pageable
    );

}
