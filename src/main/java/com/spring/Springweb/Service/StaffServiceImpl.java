package com.spring.Springweb.Service;

import com.spring.Springweb.Repository.StaffRepository;
import com.spring.Springweb.Entity.Staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffServiceImpl implements StaffService {

    @Autowired
    private StaffRepository staffRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    @Override
    public Staff createStaff(Staff staff) {
        // Check duplicate
        if (staffRepository.existsByUsername(staff.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại!");
        }
        if (staffRepository.existsByPhone(staff.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại!");
        }
        if (staffRepository.existsByEmail(staff.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại!");
        }

        // Hash password
        staff.setPasswordHash(passwordEncoder.encode(staff.getPasswordHash()));

        return staffRepository.save(staff);
    }

    @Override
    public Staff updateStaff(Integer id, Staff staffInput) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với id = " + id));

        staff.setName(staffInput.getName());
        staff.setPhone(staffInput.getPhone());
        staff.setEmail(staffInput.getEmail());
//        staff.setRole(staffInput.getRole());
        staff.setHireDate(staffInput.getHireDate());
        staff.setStatus(staffInput.getStatus());

        if (staffInput.getPasswordHash() != null && !staffInput.getPasswordHash().isBlank()) {
            staff.setPasswordHash(passwordEncoder.encode(staffInput.getPasswordHash()));
        }

        return staffRepository.save(staff);
    }

    @Override
    public Staff deleteStaff(Integer id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với id = " + id));
        staffRepository.delete(staff);
        return staff;
    }

    @Override
    public Staff getByID(Integer id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với id = " + id));
    }
    

 
}
