package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.*;
import com.spring.Springweb.Entity.*;
import com.spring.Springweb.Repository.CategoryRepository;
import com.spring.Springweb.Repository.ProductRepository;
import com.spring.Springweb.Repository.UserRepository;
import com.spring.Springweb.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ImageService imageService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final HttpServletRequest request;
    private final CategoryRepository categoryRepository;

//     🧩 Lấy danh sách tất cả sản phẩm
//     @Override
//     public List<ProductDTO> getAll() {
//         return productRepository.findAll().stream()
//                 .map(this::toDTO)
//                 .collect(Collectors.toList());
//     }
//     @Override
//     public List<ProductDTO> getAll() {
//         return productRepository.findAllWithDetailsAndUsers().stream()
//                 .map(this::toDTO)
//                 .collect(Collectors.toList());
//     }
    @Override
    public Page<ProductDTO> getAllProducts(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "id"));

        // Lấy danh sách ID
        Page<Integer> idPage = productRepository.findProductIds(pageable);
        List<Integer> ids = idPage.getContent();

        if (ids.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, idPage.getTotalElements());
        }

        // Lấy danh sách Product
        List<Product> products = productRepository.findAllWithDetailsAndUsersByIds(ids);

        Map<Integer, Product> uniqueMap = new LinkedHashMap<>();
        for (Product p : products) {
            uniqueMap.put(p.getId(), p);
        }
        products = new ArrayList<>(uniqueMap.values());
        products.sort((a, b) -> b.getId().compareTo(a.getId()));

        // 🔹 Convert sang DTO
        List<ProductDTO> dtoList = products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, idPage.getTotalElements());
    }

    // 🧩 Lấy sản phẩm theo ID
    @Override
    public ProductDTO getById(Integer id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        return toDTO(p);
    }

    // 🧩 Tạo sản phẩm mới
    @Override
    @Transactional
    public ProductDTO create(ProductDTO dto, MultipartFile mainImage) throws IOException {
        User user = getCurrentUser();

        // ✅ Upload ảnh chính
        if (mainImage != null && !mainImage.isEmpty()) {
            String imageUrl = imageService.uploadImage(mainImage);
            dto.setImageUrl(imageUrl);
            System.out.println("📸 Uploaded main image: " + imageUrl);
        }

        Product product = toEntity(dto);
        calculateDiscount(product);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        product.setCategory(category);
        product.setCreatedBy(user.getId());
        product.setUpdatedBy(user.getId());

        productRepository.save(product);
        return toDTO(product);
    }

    // 🧩 Cập nhật sản phẩm
    @Override
    @Transactional
    public ProductDTO update(Integer id, ProductDTO dto, MultipartFile mainImage) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        User user = getCurrentUser();

        // Upload ảnh mới nếu có
        if (mainImage != null && !mainImage.isEmpty()) {
            String imageUrl = imageService.uploadImage(mainImage);
            product.setImageUrl(imageUrl);
        }

        // Cập nhật thông tin cơ bản
        product.setName(dto.getName());
        product.setSku(dto.getSku());
        product.setUom(dto.getUom());
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        product.setCategory(category);

        product.setBrand(dto.getBrand());
        product.setDescription(dto.getDescription());
        product.setCostPrice(dto.getCostPrice());
        product.setSalePrice(dto.getSalePrice());
        product.setStockQty(dto.getStockQty());
        product.setReorderLevel(dto.getReorderLevel());
        product.setExpDate(dto.getExpDate());
        product.setActive(dto.getActive());
        product.setUpdatedBy(user.getId());
        product.setSize(dto.getSize());
        product.setColor(dto.getColor());
        product.setDiscountPercent(dto.getDiscountPercent());
        calculateDiscount(product);
        product.setDiscountStartDate(dto.getDiscountStartDate());
        product.setDiscountEndDate(dto.getDiscountEndDate());

        // ✅ Cập nhật chi tiết
        if (dto.getDetails() != null) {
            ProductDetails details = product.getDetails();
            if (details == null) {
                details = new ProductDetails();
                details.setProduct(product);
            }
            details.setDetailDescription(dto.getDetails().getDetailDescription());
            details.setSlug(dto.getDetails().getSlug());
            details.setOldPrice(dto.getDetails().getOldPrice());
            details.setDiscountPercent(dto.getDetails().getDiscountPercent());
            details.setIsFeatured(dto.getDetails().getIsFeatured());
            details.setTags(dto.getDetails().getTags());
            details.setBarcode(dto.getDetails().getBarcode());
            details.setLocation(dto.getDetails().getLocation());
            details.setDeleted(dto.getDetails().getDeleted());
            product.setDetails(details);
        }

        // ✅ Cập nhật gallery ảnh
        if (dto.getImages() != null) {
            product.getImages().clear();
            dto.getImages().forEach(imgDto -> {
                ProductImage img = new ProductImage();
                img.setImageUrl(imgDto.getImageUrl());
                img.setIsPrimary(imgDto.getIsPrimary());
                img.setSortOrder(imgDto.getSortOrder());
                img.setProduct(product);
                product.getImages().add(img);
            });
        }

        productRepository.save(product);
        return toDTO(product);
    }

    // 🧩 Xóa sản phẩm
    @Override
    public void delete(Integer id) {
        productRepository.deleteById(id);
    }

    // 🧩 Lấy user hiện tại từ JWT token
    private User getCurrentUser() {
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
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        } catch (Exception e) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }
    }

    // ✅ Mapping: Entity → DTO
    private ProductDTO toDTO(Product p) {
        String createdByName = null;
        String updatedByName = null;

        if (p.getCreatedBy() != null) {
            createdByName = userRepository.findById(p.getCreatedBy())
                    .map(User::getName)
                    .orElse(null);
        }

        if (p.getUpdatedBy() != null) {
            updatedByName = userRepository.findById(p.getUpdatedBy())
                    .map(User::getName)
                    .orElse(null);
        }
        return ProductDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .sku(p.getSku())
                .uom(p.getUom())
                .categoryId(
                        p.getCategory() != null ? p.getCategory().getId() : null
                )
                .brand(p.getBrand())
                .description(p.getDescription())
                .costPrice(p.getCostPrice())
                .salePrice(p.getSalePrice())
                .discountPrice(p.getDiscountPrice())
                .discountPercent(p.getDiscountPercent())
                // 👕 thời trang
                .size(p.getSize())
                .color(p.getColor())
                // 🔥 khuyến mãi
                .discountStartDate(p.getDiscountStartDate())
                .discountEndDate(p.getDiscountEndDate())
                .stockQty(p.getStockQty())
                .reorderLevel(p.getReorderLevel())
                .expDate(p.getExpDate())
                .active(p.getActive())
                .imageUrl(p.getImageUrl())
                .createdBy(p.getCreatedBy())
                .createdByName(createdByName)
                .updatedBy(p.getUpdatedBy())
                .updatedByName(updatedByName)
                .details(toDetailsDTO(p.getDetails()))
                .images(p.getImages() != null
                        ? p.getImages().stream().map(this::toImageDTO).collect(Collectors.toList())
                        : null)
                .build();
    }

    // ✅ Mapping: DTO → Entity
    private Product toEntity(ProductDTO dto) {
        Product product = Product.builder()
                .name(dto.getName())
                .sku(dto.getSku())
                .uom(dto.getUom())
                .brand(dto.getBrand())
                .description(dto.getDescription())
                .costPrice(dto.getCostPrice())
                .salePrice(dto.getSalePrice())
                .discountPrice(dto.getDiscountPrice())
                .discountPercent(dto.getDiscountPercent())
                // 👕 thời trang
                .size(dto.getSize())
                .color(dto.getColor())
                // 🔥 khuyến mãi
                .discountStartDate(dto.getDiscountStartDate())
                .discountEndDate(dto.getDiscountEndDate())
                .stockQty(dto.getStockQty())
                .reorderLevel(dto.getReorderLevel())
                .expDate(dto.getExpDate())
                .active(dto.getActive())
                .imageUrl(dto.getImageUrl())
                .build();

        if (dto.getDetails() != null) {
            ProductDetails details = toDetailsEntity(dto.getDetails());
            details.setProduct(product);
            product.setDetails(details);
        }

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<ProductImage> images = dto.getImages().stream()
                    .map(this::toImageEntity)
                    .peek(img -> img.setProduct(product))
                    .collect(Collectors.toList());
            product.setImages(images);
        }

        return product;
    }

    // ✅ Convert helpers
    private ProductDetailsDTO toDetailsDTO(ProductDetails d) {
        if (d == null) {
            return null;
        }
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
                .build();
    }

    private ProductDetails toDetailsEntity(ProductDetailsDTO dto) {
        if (dto == null) {
            return null;
        }
        return ProductDetails.builder()
                .detailDescription(dto.getDetailDescription())
                .slug(dto.getSlug())
                .oldPrice(dto.getOldPrice())
                .discountPercent(dto.getDiscountPercent())
                .isFeatured(dto.getIsFeatured())
                .tags(dto.getTags())
                .barcode(dto.getBarcode())
                .location(dto.getLocation())
                .deleted(dto.getDeleted())
                .build();
    }

    private ProductImageDTO toImageDTO(ProductImage i) {
        if (i == null) {
            return null;
        }
        return ProductImageDTO.builder()
                .id(i.getId())
                .imageUrl(i.getImageUrl())
                .isPrimary(i.getIsPrimary())
                .sortOrder(i.getSortOrder())
                .build();
    }

    private ProductImage toImageEntity(ProductImageDTO dto) {
        if (dto == null) {
            return null;
        }
        return ProductImage.builder()
                .imageUrl(dto.getImageUrl())
                .isPrimary(dto.getIsPrimary())
                .sortOrder(dto.getSortOrder())
                .build();
    }

    @Override
    public List<ProductDTO> getAll() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void calculateDiscount(Product product) {
        if (product.getSalePrice() != null && product.getDiscountPercent() != null) {

            if (product.getDiscountPercent() < 0 || product.getDiscountPercent() > 100) {
                throw new RuntimeException("Phần trăm giảm giá phải từ 0–100");
            }

            BigDecimal percent = BigDecimal
                    .valueOf(product.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100));

            BigDecimal discountPrice = product.getSalePrice()
                    .subtract(product.getSalePrice().multiply(percent));

            product.setDiscountPrice(discountPrice);
        } else {
            product.setDiscountPrice(null);
        }
    }

    @Override
    public Page<ProductDTO> getProductsByCategory(
            Integer categoryId,
            int page,
            int limit
    ) {
        Pageable pageable = PageRequest.of(
                page - 1,
                limit,
                Sort.by(Sort.Direction.DESC, "id")
        );

        // 1️⃣ Lấy ID theo category
        Page<Integer> idPage
                = productRepository.findProductIdsByCategory(categoryId, pageable);

        List<Integer> ids = idPage.getContent();

        if (ids.isEmpty()) {
            return new PageImpl<>(
                    Collections.emptyList(),
                    pageable,
                    idPage.getTotalElements()
            );
        }

        // 2️⃣ Lấy full product
        List<Product> products
                = productRepository.findAllWithDetailsAndUsersByIds(ids);

        // 3️⃣ Loại trùng + giữ thứ tự
        Map<Integer, Product> uniqueMap = new LinkedHashMap<>();
        for (Product p : products) {
            uniqueMap.put(p.getId(), p);
        }

        List<ProductDTO> dtoList = uniqueMap.values().stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(
                dtoList,
                pageable,
                idPage.getTotalElements()
        );
    }

    @Override
    public List<ProductDTO> getRelatedProducts(Integer productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        Integer categoryId = product.getCategory().getId();

        List<Product> products = productRepository.findRelatedProducts(
                categoryId,
                productId,
                PageRequest.of(0, limit) // ✅ SỬA Ở ĐÂY
        );

        return products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

}
