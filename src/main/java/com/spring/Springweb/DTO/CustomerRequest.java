/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import lombok.Data;


@Data
public class CustomerRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    
    // Getters and Setters
}
