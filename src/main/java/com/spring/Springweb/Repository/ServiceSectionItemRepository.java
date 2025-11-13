package com.spring.Springweb.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spring.Springweb.Entity.ServiceSection;
import com.spring.Springweb.Entity.ServiceSectionItem;

@Repository
public interface ServiceSectionItemRepository extends JpaRepository<ServiceSectionItem, Integer> {
    List<ServiceSectionItem> findBySectionIdOrderByExtraOrderAsc(ServiceSection section);
    boolean existsBySectionIdAndTitle(ServiceSection section, String title);
    List<ServiceSectionItem> findBySectionId(ServiceSection section);

}
