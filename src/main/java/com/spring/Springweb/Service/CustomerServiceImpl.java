/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.Entity.Customer;
import com.spring.Springweb.Repository.CustomerRepository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Customer create(Customer customer) {
        // Check phone duplicate
        if (customer.getPhone() != null && customerRepository.existsByPhone(customer.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại!");
        }

        // Check email duplicate (nếu cần)
        if (customer.getEmail() != null && customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại!");
        }

        // ✅ Mã hóa mật khẩu trước khi lưu
        if (customer.getPasswordHash() != null) {
            customer.setPasswordHash(passwordEncoder.encode(customer.getPasswordHash()));
        }

        return customerRepository.save(customer);
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
