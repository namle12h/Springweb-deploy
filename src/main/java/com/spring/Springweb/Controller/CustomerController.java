/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Controller;

import com.spring.Springweb.DTO.CustomerRequest;
import com.spring.Springweb.DTO.CustomerResponse;

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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    // private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        // Kiểm tra email đã tồn tại
        if (customerService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email đã được đăng ký"));
        }

        // Kiểm tra số điện thoại đã tồn tại
        if (customerService.existsByPhone(request.getPhone())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Số điện thoại đã được sử dụng"));
        }

        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(201).body(response);
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
