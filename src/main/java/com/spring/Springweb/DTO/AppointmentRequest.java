package com.spring.Springweb.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentRequest {

    private Integer customerId;  // có thể null nếu là guest

    @NotNull(message = "ServiceId không được để trống")
    private Integer serviceId;

    @NotBlank(message = "Ngày không được để trống")
    private String date;

    @NotBlank(message = "Giờ không được để trống")
    private String time;

    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, message = "Tên phải có ít nhất 2 ký tự")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{9,11}$", message = "Số điện thoại phải từ 9-11 chữ số")
    private String phone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(max = 255, message = "Ghi chú không được dài quá 255 ký tự")
    private String notes;

    


}
