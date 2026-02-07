/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.ServiceProductUsageDTO;
import com.spring.Springweb.Entity.Product;
import com.spring.Springweb.Entity.ServiceProductUsage;
import com.spring.Springweb.Repository.ProductRepository;
import com.spring.Springweb.Repository.ServiceProductUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceProductUsageServiceImpl implements ServiceProductUsageService {

    private final ServiceProductUsageRepository usageRepo;
    private final ProductRepository productRepo;

    @Override
    public List<ServiceProductUsageDTO> getByServiceId(Integer serviceId) {
        return usageRepo.findByServiceId(serviceId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceProductUsageDTO add(ServiceProductUsageDTO dto) {
        Product product = productRepo.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        ServiceProductUsage usage = ServiceProductUsage.builder()
                .serviceId(dto.getServiceId())
                .productId(dto.getProductId())
                .note(dto.getNote())
                .sortOrder(dto.getSortOrder())
                .build();

        ServiceProductUsage saved = usageRepo.save(usage);
        saved.setProduct(product); // Gán product để map sang DTO

        return toDTO(saved);
    }

    @Override
    public void delete(Integer id) {
        usageRepo.deleteById(id);
    }

    private ServiceProductUsageDTO toDTO(ServiceProductUsage spu) {
        return ServiceProductUsageDTO.builder()
                .id(spu.getId())
                .serviceId(spu.getServiceId())
                .productId(spu.getProductId())
                .note(spu.getNote())
                .sortOrder(spu.getSortOrder())
                .productName(spu.getProduct() != null ? spu.getProduct().getName() : null)
                .brand(spu.getProduct() != null ? spu.getProduct().getBrand() : null)
                .category(
                        spu.getProduct() != null && spu.getProduct().getCategory() != null
                        ? spu.getProduct().getCategory().getName()
                        : null
                )
                .salePrice(spu.getProduct() != null ? spu.getProduct().getSalePrice() : null)
                .imageUrl(spu.getProduct() != null ? spu.getProduct().getImageUrl() : null)
                .build();
    }
}
