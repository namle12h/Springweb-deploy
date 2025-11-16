/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.CustomerRequest;
import com.spring.Springweb.DTO.CustomerResponse;
import com.spring.Springweb.Entity.Customer;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
//    Customer create(Customer customer);

    CustomerResponse createCustomer(CustomerRequest request);

    Customer update(Integer id, Customer customer);

    void delete(Integer id);

    Customer getById(Integer id);

    List<Customer> getAll();

    Page<Customer> getAll(Pageable pageable);

    public boolean existsByEmail(String email);

    public boolean existsByPhone(String phone);

}
