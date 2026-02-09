/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.ProductImageDTO;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author ADMIN
 */
public interface ProductImageService {

    public ProductImageDTO addSubImageByFile(
            Integer productId,
            MultipartFile file,
            Integer sortOrder
    ) throws IOException;

    public List<ProductImageDTO> getImagesByProductId(Integer productId);

    // ✅ Cập nhật ảnh (đổi file ảnh)
    ProductImageDTO updateImageFile(
            Integer imageId,
            MultipartFile file
    ) throws IOException;

    // ✅ Cập nhật sortOrder
    ProductImageDTO updateSortOrder(
            Integer imageId,
            Integer sortOrder
    );

    // ✅ Xóa ảnh phụ
    void delete(Integer imageId);
}
