/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Repository;


import com.spring.Springweb.DTO.CategoryDTO;
import com.spring.Springweb.Entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByParentIsNull();

    List<Category> findByActiveTrue();

    @Query("""
SELECT new com.spring.Springweb.DTO.CategoryDTO(
    c.id,
    c.name,
    c.slug,
    COUNT(p.id)
)
FROM Category c
LEFT JOIN Product p ON p.category.id = c.id
WHERE c.active = true
GROUP BY c.id, c.name, c.slug
""")
    List<CategoryDTO> findAllWithProductCount();

}
