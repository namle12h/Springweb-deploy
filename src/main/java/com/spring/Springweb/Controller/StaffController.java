/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Controller;

import com.spring.Springweb.Entity.Staff;
import com.spring.Springweb.Service.StaffService;
import com.spring.Springweb.validation.ValidationStaff;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    @Autowired
    private StaffService staffService;

    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<Staff> getAll() {
        return staffService.getAllStaff();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Staff> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(staffService.getByID(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createStaff(
            @Valid @RequestBody ValidationStaff staffDto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        Staff staff = new Staff();
        staff.setName(staffDto.getName());
        staff.setPhone(staffDto.getPhone());
        staff.setEmail(staffDto.getEmail());
        staff.setUsername(staffDto.getUsername());
        staff.setPasswordHash(passwordEncoder.encode(staffDto.getPassword())); // OK
        staff.setStatus(staffDto.getStatus());

        return ResponseEntity.ok(staffService.createStaff(staff));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Staff staff) {
        try {
            return ResponseEntity.ok(staffService.updateStaff(id, staff));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            staffService.deleteStaff(id);
            return ResponseEntity.ok("Xóa nhân viên thành công với id = " + id);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
        ));
    }

}
