/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.validation;

import com.spring.Springweb.Entity.Customer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ValidationCustomer extends BasePersonValidation {



    public List<String> validate(Customer customer) {
        List<String> errors = new ArrayList<>();

        if (getName() == null || getName().trim().isEmpty()) {
            errors.add("Tên không được để trống");
        }

        if (getPhone() == null || !getPhone().matches("^(\\+84|0)\\d{9,10}$")) {
            errors.add("Số điện thoại không hợp lệ");
        }

        if (getDob() != null && getDob().isAfter(LocalDate.now())) {
            errors.add("Ngày sinh không được lớn hơn ngày hiện tại");
        }

        return errors;
    }
}
