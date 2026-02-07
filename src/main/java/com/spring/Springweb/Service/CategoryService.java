/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.CategoryDTO;
import com.spring.Springweb.Entity.Category;
import com.spring.Springweb.Repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Lấy tất cả danh mục
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    // Lấy danh mục cha
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIsNull();
    }

    // Tạo danh mục
    public Category create(String name, Integer parentId) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slugify(name));

        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent not found"));
            category.setParent(parent);
        }

        return categoryRepository.save(category);
    }

    private String slugify(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllWithProductCount();
    }

}
