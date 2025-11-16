/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.CustomerRequest;
import com.spring.Springweb.DTO.CustomerResponse;
import com.spring.Springweb.Entity.Customer;
import com.spring.Springweb.Repository.CustomerRepository;
import com.spring.Springweb.Repository.UserRepository;
import java.time.LocalDateTime;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private final CustomerRepository customerRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        // Kiểm tra email và số điện thoại đã tồn tại chưa
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Phone number already exists");
        }

        

        // Tạo mới đối tượng Customer
        Customer newCustomer = new Customer();
        newCustomer.setName(request.getName());
        newCustomer.setEmail(request.getEmail());
        newCustomer.setPhone(request.getPhone());
        newCustomer.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Mã hóa mật khẩu
        newCustomer.setRole("CUSTOMER");
        newCustomer.setCreatedAt(LocalDateTime.now());
        newCustomer.setUsername(request.getEmail());

        // Lưu khách hàng mới vào cơ sở dữ liệu
        userRepository.save(newCustomer);

        // Trả về phản hồi
        return new CustomerResponse(newCustomer.getId(), newCustomer.getName(), newCustomer.getEmail());
    }

    @Override
    public Customer update(Integer id, Customer customer) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        existing.setName(customer.getName());
        existing.setPhone(customer.getPhone());
        existing.setEmail(customer.getEmail());
        existing.setDob(customer.getDob());
        // ✅ Mã hóa mật khẩu trước khi lưu
        if (customer.getPasswordHash() != null) {
            customer.setPasswordHash(passwordEncoder.encode(customer.getPasswordHash()));
        }
        return customerRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        customerRepository.deleteById(id);
    }

    @Override
    public Customer getById(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @Override
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    @Override
    public Page<Customer> getAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    public boolean existsByEmail(String email) {
        return customerRepository.findByEmail(email).isPresent();
    }

    public boolean existsByPhone(String phone) {
        return customerRepository.findByPhone(phone).isPresent();
    }

}
