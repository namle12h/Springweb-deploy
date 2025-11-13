package com.spring.Springweb.Controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.spring.Springweb.DTO.ServiceSectionDTO;
import com.spring.Springweb.DTO.ServiceSectionItemDTO;
import com.spring.Springweb.Service.ServiceSectionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceSectionController {

    private final ServiceSectionService serviceSectionService;

    // GET /api/services/{id}/sections?type=step
    @GetMapping("/{id}/sections")
    public ResponseEntity<List<ServiceSectionDTO>> getSections(
            @PathVariable Integer id,
            @RequestParam(required = false) String type
    ) {
        return ResponseEntity.ok(serviceSectionService.getSectionsByService(id, type));
    }

    @PostMapping(value = "/{id}/sections/{type}")

    public ResponseEntity<ServiceSectionItemDTO> addItem(
            @PathVariable Integer id,
            @PathVariable String type,
            @RequestPart("dto") ServiceSectionItemDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "beforeImage", required = false) MultipartFile beforeImage,
            @RequestPart(value = "afterImage", required = false) MultipartFile afterImage
    ) throws IOException {
        ServiceSectionItemDTO result = serviceSectionService.addSectionItems(id, type, dto, file, beforeImage, afterImage);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/sections/{type}")
    public ResponseEntity<?> getSectionItems(
            @PathVariable Integer id,
            @PathVariable String type
    ) {
        return ResponseEntity.ok(serviceSectionService.getSectionItems(id, type));
    }
}
