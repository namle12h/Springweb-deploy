package com.spring.Springweb.Controller;

import com.spring.Springweb.DTO.ProductDetailsDTO;
import com.spring.Springweb.Service.ProductDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/{productId}/details")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductDetailsController {

    private final ProductDetailsService productDetailsService;

    // ✅ Lấy chi tiết sản phẩm
    @GetMapping
    public ResponseEntity<ProductDetailsDTO> getDetails(@PathVariable Integer productId) {
        return ResponseEntity.ok(productDetailsService.getDetailsByProductId(productId));
    }

    // ✅ Tạo hoặc cập nhật chi tiết
    @PostMapping
    public ResponseEntity<ProductDetailsDTO> createOrUpdate(
            @PathVariable Integer productId,
            @RequestBody ProductDetailsDTO dto
    ) {
        return ResponseEntity.ok(productDetailsService.saveOrUpdate(productId, dto));
    }

    // ✅ Xóa chi tiết
    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Integer productId) {
        productDetailsService.deleteByProductId(productId);
        return ResponseEntity.noContent().build();
    }
}
