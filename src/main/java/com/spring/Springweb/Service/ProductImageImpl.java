/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.ProductImageDTO;
import com.spring.Springweb.Entity.Product;
import com.spring.Springweb.Entity.ProductImage;
import com.spring.Springweb.Entity.User;
import com.spring.Springweb.Repository.ProductImageRepository;
import com.spring.Springweb.Repository.ProductRepository;
import com.spring.Springweb.Repository.UserRepository;
import com.spring.Springweb.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author ADMIN
 */
@Service
@RequiredArgsConstructor
public class ProductImageImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ImageService imageService; // 🔥 dùng lại
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    @Transactional
    @Override
    public ProductImageDTO addSubImageByFile(
            Integer productId,
            MultipartFile file,
            Integer sortOrder
    ) throws IOException {

        // 🔐 CHECK USER + QUYỀN
        User user = getCurrentUserAndCheckPermission();

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File ảnh không được để trống");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // 📤 Upload ảnh
        String imageUrl = imageService.uploadImage(file);
        System.out.println("📸 Uploaded sub image by " + user.getUsername() + ": " + imageUrl);

        ProductImage image = ProductImage.builder()
                .imageUrl(imageUrl)
                .isPrimary(false)
                .sortOrder(sortOrder)
                .product(product)
                .build();

        productImageRepository.save(image);

        return ProductImageDTO.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .isPrimary(false)
                .sortOrder(image.getSortOrder())
                .build();
    }

    // 🔐 HÀM CHECK QUYỀN
    private User getCurrentUserAndCheckPermission() {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Thiếu token xác thực");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new RuntimeException("Token rỗng hoặc sai định dạng");
        }

        try {
            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            if (!"ADMIN".equals(user.getRole()) && !"STAFF".equals(user.getRole())) {
                throw new RuntimeException("Bạn không có quyền thêm ảnh sản phẩm");
            }

            return user;
        } catch (Exception e) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }
    }

    @Transactional
    @Override
    public ProductImageDTO updateImageFile(
            Integer imageId,
            MultipartFile file
    ) throws IOException {

        getCurrentUserAndCheckPermission();

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File ảnh không được để trống");
        }

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

        // 📤 upload ảnh mới (GIỐNG addSubImage)
        String imageUrl = imageService.uploadImage(file);

        image.setImageUrl(imageUrl);

        productImageRepository.save(image);

        return mapToDTO(image);
    }

    @Transactional
    @Override
    public ProductImageDTO updateSortOrder(
            Integer imageId,
            Integer sortOrder
    ) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

        image.setSortOrder(sortOrder);
        productImageRepository.save(image);

        return mapToDTO(image);
    }

    @Override
    public List<ProductImageDTO> getImagesByProductId(Integer productId) {

        List<ProductImage> images
                = productImageRepository.findByProductIdOrderBySortOrderAsc(productId);

        return images.stream()
                .map(image -> ProductImageDTO.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .isPrimary(image.getIsPrimary())
                .sortOrder(image.getSortOrder())
                .build()
                )
                .toList();
    }

    @Transactional
    @Override
    public void delete(Integer imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

        productImageRepository.delete(image);
    }

    // ================== MAPPER ==================
    private ProductImageDTO mapToDTO(ProductImage image) {
        return ProductImageDTO.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .isPrimary(image.getIsPrimary())
                .sortOrder(image.getSortOrder())
                .build();
    }

}
