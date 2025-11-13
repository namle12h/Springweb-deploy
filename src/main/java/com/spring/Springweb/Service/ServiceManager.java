/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.spring.Springweb.Entity.ServiceEntity;

@Service
public interface ServiceManager {

    public List<ServiceEntity> getAllServices();

    public Optional<ServiceEntity> getServiceById(Integer id);

    public ServiceEntity createService(ServiceEntity service);

    public ServiceEntity updateService(Integer id, ServiceEntity serviceDetails);

    public void deleteService(Integer id);

    public String uploadImage(MultipartFile file) throws IOException;

    public ServiceEntity UploadServiceImage(Integer id, MultipartFile file) throws IOException;
}
