package com.spring.Springweb.Repository;

import com.spring.Springweb.Entity.SectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionTypeRepository extends JpaRepository<SectionType, String> {
}
