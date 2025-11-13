/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.Repository.ServiceRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.spring.Springweb.Entity.ServiceEntity;
import com.spring.Springweb.Entity.User;
import com.spring.Springweb.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author ADMIN
 */
@Service
@RequiredArgsConstructor
public class ServiceManagerImpl implements ServiceManager {

    @Autowired
    private ServiceRepository serviceRepository;
    private final ImageService imageService;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    @Override
    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAll();
    }

    @Override
    public Optional<ServiceEntity> getServiceById(Integer id) {
        return serviceRepository.findById(id);
    }

    @Override
    public ServiceEntity createService(ServiceEntity service) {
        // Lấy thông tin user hiện tại
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;

        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            userId = currentUser.getId().longValue();
        }

        // Lưu service
        ServiceEntity saved = serviceRepository.save(service);

        // Audit log
        auditLogService.log(
                "Service",
                saved.getId().longValue(),
                "CREATE",
                null,
                null,
                String.format("name=%s, price=%s, duration=%s",
                        saved.getName(),
                        saved.getPrice(),
                        saved.getDurationMin()),
                userId
        );

        return saved;
    }

    @Override
    @Transactional
    public ServiceEntity updateService(Integer id, ServiceEntity serviceDetails) {
        ServiceEntity existing = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id " + id));

        Long userId = getCurrentUserId();

        if (serviceDetails.getName() != null && !serviceDetails.getName().equals(existing.getName())) {
            auditLogService.log("Service", id.longValue(), "UPDATE",
                    "Name", existing.getName(), serviceDetails.getName(), userId);
            existing.setName(serviceDetails.getName());
        }

        if (serviceDetails.getPrice() != null && !serviceDetails.getPrice().equals(existing.getPrice())) {
            auditLogService.log("Service", id.longValue(), "UPDATE",
                    "Price", String.valueOf(existing.getPrice()), String.valueOf(serviceDetails.getPrice()), userId);
            existing.setPrice(serviceDetails.getPrice());
        }

        if (serviceDetails.getDurationMin() > 0 && serviceDetails.getDurationMin() != existing.getDurationMin()) {
            auditLogService.log("Service", id.longValue(), "UPDATE",
                    "DurationMin", String.valueOf(existing.getDurationMin()), String.valueOf(serviceDetails.getDurationMin()), userId);
            existing.setDurationMin(serviceDetails.getDurationMin());
        }

        if (serviceDetails.getActive() != null && !serviceDetails.getActive().equals(existing.getActive())) {
            auditLogService.log("Service", id.longValue(), "UPDATE",
                    "Active", String.valueOf(existing.getActive()), String.valueOf(serviceDetails.getActive()), userId);
            existing.setActive(serviceDetails.getActive());
        }

        if (serviceDetails.getDescription() != null && !serviceDetails.getDescription().equals(existing.getDescription())) {
            auditLogService.log("Service", id.longValue(), "UPDATE",
                    "Description", existing.getDescription(), serviceDetails.getDescription(), userId);
            existing.setDescription(serviceDetails.getDescription());
        }

        if (serviceDetails.getImageUrl() != null && !serviceDetails.getImageUrl().equals(existing.getImageUrl())) {
            auditLogService.log("Service", id.longValue(), "UPDATE",
                    "ImageUrl", existing.getImageUrl(), serviceDetails.getImageUrl(), userId);
            existing.setImageUrl(serviceDetails.getImageUrl());
        }

        return serviceRepository.save(existing);
    }

    @Override
    public void deleteService(Integer id) {

        ServiceEntity existing = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Long userId = getCurrentUserId();

        // log dữ liệu cũ trước khi xoá
        auditLogService.log(
                "Service",
                id.longValue(),
                "DELETE",
                null,
                String.format("name=%s, price=%s, duration=%s",
                        existing.getName(),
                        existing.getPrice(),
                        existing.getDurationMin()),
                null,
                userId);
        serviceRepository.deleteById(id);
    }

    @Override
    public ServiceEntity UploadServiceImage(Integer id, MultipartFile file) throws IOException {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        String url = imageService.uploadImage(file);
        service.setImageUrl(url);
        return serviceRepository.save(service);
    }

    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        return imageService.uploadImage(file);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            return currentUser.getId().longValue();
        }
        throw new RuntimeException("Unauthorized: user not logged in");
    }

}
