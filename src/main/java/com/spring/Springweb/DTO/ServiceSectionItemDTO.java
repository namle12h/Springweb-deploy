package com.spring.Springweb.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceSectionItemDTO {

    private Integer id;
    private String title;
    private String description;
    private Integer times;
    private String imageUrl;
    private Integer extraOrder;
    private String extraData;
}
