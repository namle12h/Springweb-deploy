package com.spring.Springweb.Repository;

import com.spring.Springweb.Entity.ServiceSection;
import com.spring.Springweb.Entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceSectionRepository extends JpaRepository<ServiceSection, Integer> {
    List<ServiceSection> findByServiceId(ServiceEntity service);
    List<ServiceSection> findByServiceIdAndSectionType_Code(ServiceEntity service, String sectionType);
}
