/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Repository;


import com.spring.Springweb.Entity.ServiceProductUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceProductUsageRepository extends JpaRepository<ServiceProductUsage, Integer> {

    @Query("""
        SELECT spu FROM ServiceProductUsage spu
        JOIN FETCH spu.product p
        WHERE spu.serviceId = :serviceId
        ORDER BY spu.sortOrder ASC
    """)
    List<ServiceProductUsage> findByServiceId(Integer serviceId);

}
