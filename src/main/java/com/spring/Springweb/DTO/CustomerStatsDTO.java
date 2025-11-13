/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatsDTO {
    private long newCustomers;            // Số khách hàng mới
    private long returningCustomers;      // Số khách hàng quay lại
    private long vipCustomers;            // Số khách hàng VIP
    private Map<String, Long> ageGroups;  // Phân nhóm theo độ tuổi (18-24, 25-34, 35-50, ...)
}
