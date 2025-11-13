package com.spring.Springweb.Repository;

import com.spring.Springweb.Entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Integer> {

    Optional<Staff> findByUsername(String username);

    Optional<Staff> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    public boolean existsByUsername(String username);
}
