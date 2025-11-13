package com.spring.Springweb.validation;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationStaff extends BasePersonValidation {

    @NotBlank(message = "Vai trò không được để trống")
    @Pattern(regexp = "ADMIN|DOCTOR|THERAPIST|RECEPTIONIST|CASHIER", 
             message = "Vai trò chỉ hợp lệ: ADMIN, DOCTOR, THERAPIST, RECEPTIONIST, CASHIER")
    private String role;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4-50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6-100 ký tự")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
        message = "Mật khẩu phải chứa ít nhất 1 chữ thường, 1 chữ hoa, 1 số và 1 ký tự đặc biệt"
    )
    private String password;

    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Trạng thái chỉ có thể là ACTIVE hoặc INACTIVE")
    private String status;
    
    
    
}
