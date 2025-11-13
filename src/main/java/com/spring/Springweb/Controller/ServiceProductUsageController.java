/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Controller;


import com.spring.Springweb.DTO.ServiceProductUsageDTO;
import com.spring.Springweb.Service.ServiceProductUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceProductUsageController {

    private final ServiceProductUsageService usageService;

    @GetMapping("/{serviceId}/products")
    public ResponseEntity<List<ServiceProductUsageDTO>> getProducts(@PathVariable Integer serviceId) {
        return ResponseEntity.ok(usageService.getByServiceId(serviceId));
    }

    @PostMapping("/{serviceId}/products")
    public ResponseEntity<ServiceProductUsageDTO> addProduct(
            @PathVariable Integer serviceId,
            @RequestBody ServiceProductUsageDTO dto) {
        dto.setServiceId(serviceId);
        return ResponseEntity.ok(usageService.add(dto));
    }

    @DeleteMapping("/{serviceId}/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        usageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
