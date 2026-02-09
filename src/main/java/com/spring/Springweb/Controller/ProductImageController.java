package com.spring.Springweb.Controller;

import com.spring.Springweb.DTO.ProductImageDTO;
import com.spring.Springweb.Service.ProductImageService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping(
            value = "/{productId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductImageDTO> uploadSubImage(
            @PathVariable Integer productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder
    ) throws IOException {

        return ResponseEntity.ok(
                productImageService.addSubImageByFile(productId, file, sortOrder)
        );
    }

    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImageDTO>> getSubImages(
            @PathVariable Integer productId
    ) {
        return ResponseEntity.ok(
                productImageService.getImagesByProductId(productId)
        );
    }

    // ✏️ Cập nhật ảnh
    @PutMapping(
            value = "/product-images/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductImageDTO> updateImage(
            @PathVariable Integer id,
            @RequestPart("file") MultipartFile file
    ) throws IOException {

        return ResponseEntity.ok(
                productImageService.updateImageFile(id, file)
        );
    }

    // 🔁 Cập nhật sortOrder
    @PatchMapping("/product-images/{id}/sort-order")
    public ResponseEntity<ProductImageDTO> updateSortOrder(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body
    ) {
        return ResponseEntity.ok(
                productImageService.updateSortOrder(id, body.get("sortOrder"))
        );
    }

    // 🗑️ Xóa ảnh phụ
    @DeleteMapping("/product-images/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Integer id) {
        productImageService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
