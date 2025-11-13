package com.spring.Springweb.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.spring.Springweb.Entity.ServiceEntity;
import com.spring.Springweb.Service.ServiceManager;
import java.io.IOException;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceManager serviceManager;

    // Lấy tất cả dịch vụ
    @GetMapping
    public List<ServiceEntity> getAllServices() {
        return serviceManager.getAllServices();
    }

    // Lấy dịch vụ theo ID
    @GetMapping("/{id}")
    public ServiceEntity getServiceById(@PathVariable Integer id) {
        return serviceManager.getServiceById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id " + id));
    }

    // Tạo mới dịch vụ
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ServiceEntity createService(@RequestBody ServiceEntity service) {
        return serviceManager.createService(service);
    }

    @PostMapping("/with-image")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ServiceEntity createServiceWithImage(
            @RequestPart("service") ServiceEntity service,
            //            @PathVariable Integer id,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {

            String url = serviceManager.uploadImage(file);

            service.setImageUrl(url);
        }
        return serviceManager.createService(service);
    }

    @PutMapping("/{id}/with-image")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ServiceEntity updateServiceWithImage(
            @PathVariable Integer id,
            @RequestPart("service") ServiceEntity serviceDetails,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        if (file != null && !file.isEmpty()) {
            String url = serviceManager.uploadImage(file); // upload lên Cloudinary
            serviceDetails.setImageUrl(url); // gán URL mới
        }

        return serviceManager.updateService(id, serviceDetails);
    }
    // Cập nhật dịch vụ
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ServiceEntity updateService(@PathVariable Integer id, @RequestBody ServiceEntity serviceDetails) {
        return serviceManager.updateService(id, serviceDetails);
    }

    // Xóa dịch vụ
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String deleteService(@PathVariable Integer id) {
        serviceManager.deleteService(id);
        return "Service with ID " + id + " has been deleted";
    }

    

}
