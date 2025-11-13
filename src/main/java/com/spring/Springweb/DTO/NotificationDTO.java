/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class NotificationDTO {

    private String title;
    private String message;
    private String type;
    private Long targetId;
    private String entityType;
    private Long entityId;
    private boolean isDeleted = false;
}
