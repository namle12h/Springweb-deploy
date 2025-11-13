/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.Springweb.DTO.ServiceSectionDTO;
import com.spring.Springweb.DTO.ServiceSectionItemDTO;
import com.spring.Springweb.Entity.SectionType;
import com.spring.Springweb.Entity.ServiceEntity;
import com.spring.Springweb.Entity.ServiceSection;
import com.spring.Springweb.Entity.ServiceSectionItem;
import com.spring.Springweb.Repository.SectionTypeRepository;
import com.spring.Springweb.Repository.ServiceRepository;
import com.spring.Springweb.Repository.ServiceSectionItemRepository;
import com.spring.Springweb.Repository.ServiceSectionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceSectionServiceImpl implements ServiceSectionService {

    private final ServiceSectionRepository sectionRepo;
    private final ServiceSectionItemRepository itemRepo;
    private final ServiceRepository serviceRepo;
    private final SectionTypeRepository sectionTypeRepo;
    private final ImageService imageService;

    @Override
    public List<ServiceSectionDTO> getSectionsByService(Integer serviceId, String type) {
        ServiceEntity service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        List<ServiceSection> sections = (type != null && !type.isEmpty())
                ? sectionRepo.findByServiceIdAndSectionType_Code(service, type)
                : sectionRepo.findByServiceId(service);

        return sections.stream().map(section -> {
            List<ServiceSectionItemDTO> items = itemRepo.findBySectionIdOrderByExtraOrderAsc(section)
                    .stream()
                    .map(i -> ServiceSectionItemDTO.builder()
                    .id(i.getId())
                    .title(i.getTitle())
                    .description(i.getDescription())
                    .times(i.getTimes())
                    .imageUrl(i.getImageUrl())
                    .extraOrder(i.getExtraOrder())
                    .extraData(i.getExtraData())
                    .build()
                    )
                    .collect(Collectors.toList());

            return ServiceSectionDTO.builder()
                    .sectionType(section.getSectionType().getCode())
                    .title(section.getTitle())
                    .items(items)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ServiceSectionItemDTO addSectionItem(Integer serviceId, String type, ServiceSectionItemDTO dto) {
        ServiceEntity service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        List<ServiceSection> sections = sectionRepo.findByServiceIdAndSectionType_Code(service, type);
        ServiceSection section;

        if (sections.isEmpty()) {
            SectionType sectionType = sectionTypeRepo.findById(type)
                    .orElseThrow(() -> new EntityNotFoundException("SectionType not found: " + type));

            section = new ServiceSection();
            section.setServiceId(service);
            section.setSectionType(sectionType);
            section.setTitle(sectionType.getDescription());
            section.setSortOrder(1);
            section = sectionRepo.save(section);
        } else {
            section = sections.get(0);
        }

        ServiceSectionItem item = new ServiceSectionItem();
        item.setSectionId(section);
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setTimes(dto.getTimes());
        item.setImageUrl(dto.getImageUrl());
        item.setExtraOrder(dto.getExtraOrder());
        itemRepo.save(item);

        dto.setId(item.getId());
        return dto;
    }

    @Override
    @Transactional
    public ServiceSectionItemDTO addSectionItems(
            Integer serviceId,
            String type,
            ServiceSectionItemDTO dto,
            MultipartFile file,
            MultipartFile beforeImage,
            MultipartFile afterImage
    ) throws IOException {

        // 1️⃣ Tìm Service
        ServiceEntity service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found with id " + serviceId));

        // 2️⃣ Tìm hoặc tạo mới Section
        List<ServiceSection> sections = sectionRepo.findByServiceIdAndSectionType_Code(service, type);
        ServiceSection section;

        if (sections.isEmpty()) {
            SectionType sectionType = sectionTypeRepo.findById(type)
                    .orElseThrow(() -> new EntityNotFoundException("SectionType not found: " + type));

            section = new ServiceSection();
            section.setServiceId(service);
            section.setSectionType(sectionType);
            section.setTitle(sectionType.getDescription());
            section.setSortOrder(1);
            section = sectionRepo.save(section);
        } else {
            section = sections.get(0);
        }

        // 3️⃣ Upload ảnh chính nếu có
        String imageUrl = dto.getImageUrl();
        if (file != null && !file.isEmpty()) {
            try {
                imageUrl = imageService.uploadImage(file);
            } catch (IOException e) {
                throw new IOException("Upload image failed: " + e.getMessage(), e);
            }
        }

        // 4️⃣ Upload ảnh before/after nếu có
        String beforeUrl = null;
        String afterUrl = null;

        if (beforeImage != null && !beforeImage.isEmpty()) {
            beforeUrl = imageService.uploadImage(beforeImage);
        }
        if (afterImage != null && !afterImage.isEmpty()) {
            afterUrl = imageService.uploadImage(afterImage);
        }

        // 5️⃣ Xử lý extraData (merge link ảnh thật vào JSON)
        String updatedExtraData = dto.getExtraData();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode extraNode = mapper.createObjectNode();

        if (updatedExtraData != null && !updatedExtraData.isBlank()) {
            extraNode = (ObjectNode) mapper.readTree(updatedExtraData);
        }

        // ⚠️ Xóa base64 cũ, thêm URL thật
//        extraNode.remove("before");
//        extraNode.remove("after");
        if (beforeUrl != null) {
            extraNode.put("before", beforeUrl);
        }
        if (afterUrl != null) {
            extraNode.put("after", afterUrl);
        }

        updatedExtraData = extraNode.toString();

        // 6️⃣ Check trùng tiêu đề
        boolean exists = itemRepo.existsBySectionIdAndTitle(section, dto.getTitle());
//        if (exists) {
//            throw new IllegalArgumentException("Bước này đã tồn tại trong quy trình!");
//        }

        // 7️⃣ Tạo item mới
        ServiceSectionItem item = new ServiceSectionItem();
        item.setSectionId(section);
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setTimes(dto.getTimes());
        item.setExtraOrder(dto.getExtraOrder());
        item.setImageUrl(imageUrl);
        item.setExtraData(updatedExtraData);

        itemRepo.save(item);

        // 8️⃣ Trả về DTO
        dto.setId(item.getId());
        dto.setImageUrl(imageUrl);
        dto.setExtraData(updatedExtraData);
        System.out.println("Before file: " + (beforeImage != null ? beforeImage.getOriginalFilename() : "null"));
        System.out.println("After file: " + (afterImage != null ? afterImage.getOriginalFilename() : "null"));

        return dto;
    }

    @Override
    public List<ServiceSectionItemDTO> getSectionItems(Integer serviceId, String type) {
        ServiceEntity service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        List<ServiceSection> sections = sectionRepo.findByServiceIdAndSectionType_Code(service, type);
        if (sections.isEmpty()) {
            return List.of();
        }

        ServiceSection section = sections.get(0);
        List<ServiceSectionItem> items = itemRepo.findBySectionId(section);

        return items.stream().map(item -> {
            ServiceSectionItemDTO dto = new ServiceSectionItemDTO();
            dto.setId(item.getId());
            dto.setTitle(item.getTitle());
            dto.setDescription(item.getDescription());
            dto.setTimes(item.getTimes());
            dto.setImageUrl(item.getImageUrl());
            dto.setExtraOrder(item.getExtraOrder());
            dto.setExtraData(item.getExtraData());
            return dto;
        }).toList();
    }

}
