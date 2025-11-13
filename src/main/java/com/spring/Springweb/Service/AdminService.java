
package com.spring.Springweb.Service;

import com.spring.Springweb.Entity.Admin;
import com.spring.Springweb.Repository.AdminRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public Admin create(Admin admin) {
        if (admin.getPasswordHash() != null) {
            admin.setPasswordHash(passwordEncoder.encode(admin.getPasswordHash()));
        }
        return adminRepository.save(admin);
    }

    public Admin update(Integer id, Admin update) {
        Admin existing = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        existing.setName(update.getName());
        existing.setPhone(update.getPhone());
        existing.setEmail(update.getEmail());
        return adminRepository.save(existing);
    }

    public void delete(Integer id) {
        adminRepository.deleteById(id);
    }

    public List<Admin> getAll() {
        return adminRepository.findAll();
    }

    public Admin getById(Integer id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }
}
