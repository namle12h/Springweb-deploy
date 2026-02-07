/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Controller;

import com.spring.Springweb.DTO.CategoryDTO;
import com.spring.Springweb.DTO.CreateCategoryRequest;
import com.spring.Springweb.Entity.Category;
import com.spring.Springweb.Service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // GET /api/categories
    @GetMapping
    public List<Category> getAll() {
        return categoryService.getAll();
    }

    // GET /api/categories/root
    @GetMapping("/root")
    public List<Category> getRoot() {
        return categoryService.getRootCategories();
    }

    // POST /api/categories
    @PostMapping
    public Category create(@RequestBody CreateCategoryRequest request) {
        return categoryService.create(request.getName(), request.getParentId());
    }

    @GetMapping("/quantity")
    public List<CategoryDTO> getCategories() {
        return categoryService.getAllCategories();
    }

}
