/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.ServiceProductUsageDTO;
import com.spring.Springweb.Entity.ServiceProductUsage;
import java.util.List;

/**
 *
 * @author ADMIN
 */
public interface ServiceProductUsageService {

    public List<ServiceProductUsageDTO> getByServiceId(Integer serviceId);

    public ServiceProductUsageDTO add(ServiceProductUsageDTO dto);

    public void delete(Integer id);

}
