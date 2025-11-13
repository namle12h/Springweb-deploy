package com.spring.Springweb.Service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.spring.Springweb.DTO.ServiceSectionDTO;
import com.spring.Springweb.DTO.ServiceSectionItemDTO;

public interface ServiceSectionService {

    List<ServiceSectionDTO> getSectionsByService(Integer serviceId, String type);

    ServiceSectionItemDTO addSectionItem(Integer serviceId, String type, ServiceSectionItemDTO dto);

    ServiceSectionItemDTO addSectionItems(Integer serviceId, String type, ServiceSectionItemDTO dto, MultipartFile file, MultipartFile beforeImage, MultipartFile afterImage  ) throws IOException;

    List<ServiceSectionItemDTO> getSectionItems(Integer serviceId, String type);
}
