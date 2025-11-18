package com.spring.Springweb.Controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.spring.Springweb.DTO.ProductDTO;
import com.spring.Springweb.Entity.Product;
import com.spring.Springweb.Service.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Page<ProductDTO> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return productService.getAllProducts(page, limit);
    }

    // ✅ Lấy chi tiết 1 sản phẩm
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) {
        ProductDTO product = productService.getById(id);
        return ResponseEntity.ok(product);
    }

    // ✅ Thêm sản phẩm (multipart: product JSON + image)
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ProductDTO> createProduct(
            @RequestPart("product") ProductDTO productDto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {

        ProductDTO created = productService.create(productDto, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ✅ Cập nhật sản phẩm (multipart: product JSON + optional image)
    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Integer id,
            @RequestPart("product") ProductDTO productDto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {

        ProductDTO updated = productService.update(id, productDto, image);
        return ResponseEntity.ok(updated);
    }

    // ✅ Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
