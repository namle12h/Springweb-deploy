/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TimeSlotStatDTO {
    
    private String timeSlot;  // Khung giờ (VD: "8:00-10:00", "10:00-12:00", ...)
    private Long appointments; // Số cuộc hẹn trong khung giờ đó
}
