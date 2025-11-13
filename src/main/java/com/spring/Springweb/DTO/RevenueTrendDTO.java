/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevenueTrendDTO {

    private String label;     // Tuần này / Tuần trước / ...
    private String amount;    // "28.5M"
    private Double change;    // 12.5
    private boolean isUp;     // true / false

}
