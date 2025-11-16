/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.validation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BasePersonValidation {

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 200, message = "Tên tối đa 200 ký tự")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(\\+84|0)\\d{9,10}$", message = "Số điện thoại không hợp lệ, phải bắt đầu bằng +84 hoặc 0")
    private String phone;

    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Password không được để trống")
    private String password;

    @PastOrPresent(message = "Ngày sinh không được lớn hơn ngày hiện tại")
    private LocalDate dob;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String notes;

       @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String username;
}
