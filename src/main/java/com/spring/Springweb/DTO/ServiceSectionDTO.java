package com.spring.Springweb.DTO;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceSectionDTO {
    private String sectionType; // step, benefit, note...
    private String title;
    private List<ServiceSectionItemDTO> items;
}
