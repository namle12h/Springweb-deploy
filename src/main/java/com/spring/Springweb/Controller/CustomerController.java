/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.Springweb.Entity.Customer;
import com.spring.Springweb.Entity.User;
import com.spring.Springweb.Repository.UserRepository;
import com.spring.Springweb.Service.CustomerService;
import com.spring.Springweb.util.JwtUtil;
import com.spring.Springweb.validation.ValidationCustomer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // @PostMapping
    // public ResponseEntity<Customer> create(@RequestBody Customer customer) {
    //     return ResponseEntity.ok(customerService.create(customer));
    // }
    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody ValidationCustomer customerDto,
            BindingResult bindingResult) {

        // ❌ Validate lỗi từ DTO
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        // 🔍 Kiểm tra email trùng
        if (customerService.existsByEmail(customerDto.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email đã được đăng ký"));
        }

        // 🔍 Kiểm tra số điện thoại trùng (nếu cần)
        if (customerService.existsByPhone(customerDto.getPhone())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Số điện thoại đã được sử dụng"));
        }

        // 🧩 Tạo mới Customer
        Customer customer = new Customer();
        customer.setName(customerDto.getName());
        customer.setPhone(customerDto.getPhone());
        customer.setEmail(customerDto.getEmail());

        // 🔒 Mã hoá mật khẩu
        customer.setPasswordHash(passwordEncoder.encode(customerDto.getPassword()));

        if (customerDto.getDob() != null) {
            customer.setDob(customerDto.getDob());
        }

        // 📅 createdAt sẽ tự động set trong @PrePersist
        Customer saved = customerService.create(customer);

        return ResponseEntity.ok(Map.of(
                "message", "Đăng ký thành công!",
                "customer", Map.of(
                        "id", saved.getId(),
                        "name", saved.getName(),
                        "email", saved.getEmail(),
                        "phone", saved.getPhone()
                )
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(@PathVariable Integer id, @RequestBody Customer customer) {
        return ResponseEntity.ok(customerService.update(id, customer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

//    @GetMapping
//    public ResponseEntity<List<Customer>> getAll() {
//        return ResponseEntity.ok(customerService.getAll());
//    }
    @GetMapping
    public Page<Customer> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return customerService.getAll(pageable);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getCustomerProfile(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Thiếu token");
        }

        String token = authHeader.substring(7);
        String subject = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmailOrUsername(subject, subject)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (!(user instanceof Customer)) {
            return ResponseEntity.status(403).body("Không có quyền");
        }

        return ResponseEntity.ok(user);  // return trực tiếp entity Customer
    }

}
