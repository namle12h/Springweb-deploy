/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.ProductDetailsDTO;
import com.spring.Springweb.Entity.Product;
import com.spring.Springweb.Entity.ProductDetails;
import com.spring.Springweb.Repository.ProductDetailsRepository;
import com.spring.Springweb.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductDetailsServiceImpl implements ProductDetailsService {

    private final ProductDetailsRepository productDetailsRepository;
    private final ProductRepository productRepository;

    // ✅ Lấy chi tiết theo Product ID
    @Override
    public ProductDetailsDTO getDetailsByProductId(Integer productId) {
        ProductDetails details = productDetailsRepository.findByProductId(productId);
        if (details == null) {
            throw new RuntimeException("Chưa có chi tiết cho sản phẩm ID: " + productId);
        }
        return toDTO(details);
    }

    // ✅ Tạo hoặc cập nhật chi tiết sản phẩm
    @Override
    @Transactional
    public ProductDetailsDTO saveOrUpdate(Integer productId, ProductDetailsDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + productId));

        ProductDetails details = productDetailsRepository.findByProductId(productId);
        if (details == null) {
            details = new ProductDetails();
            details.setProduct(product);
        }

        details.setDetailDescription(dto.getDetailDescription());
        details.setSlug(dto.getSlug());
        details.setOldPrice(dto.getOldPrice());
        details.setDiscountPercent(dto.getDiscountPercent());
        details.setIsFeatured(dto.getIsFeatured());
        details.setTags(dto.getTags());
        details.setBarcode(dto.getBarcode());
        details.setLocation(dto.getLocation());
        details.setDeleted(dto.getDeleted());

        ProductDetails saved = productDetailsRepository.save(details);
        return toDTO(saved);
    }

    // ✅ Xóa chi tiết
    @Override
    public void deleteByProductId(Integer productId) {
        ProductDetails details = productDetailsRepository.findByProductId(productId);
        if (details != null) {
            productDetailsRepository.delete(details);
        }
    }

    // Mapping Entity ↔ DTO
    private ProductDetailsDTO toDTO(ProductDetails d) {
        return ProductDetailsDTO.builder()
                .id(d.getId())
                .detailDescription(d.getDetailDescription())
                .slug(d.getSlug())
                .oldPrice(d.getOldPrice())
                .discountPercent(d.getDiscountPercent())
                .isFeatured(d.getIsFeatured())
                .tags(d.getTags())
                .barcode(d.getBarcode())
                .location(d.getLocation())
                .deleted(d.getDeleted())
//                .images(d.getimage)
                .build();
    }
}
