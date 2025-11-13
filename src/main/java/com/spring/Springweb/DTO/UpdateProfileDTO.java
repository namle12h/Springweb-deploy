package com.spring.Springweb.DTO;

import java.time.LocalDate;
import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String name;
    private String phone;
    private String address;
    private String gender;
    private LocalDate dob;
}
