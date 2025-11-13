/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.spring.Springweb.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetricDTO {
    private double current;
    private double previous;
    private double changePercent;
    private boolean up;
}
