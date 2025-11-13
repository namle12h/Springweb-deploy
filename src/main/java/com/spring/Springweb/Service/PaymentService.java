/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.PaymentRequest;
import com.spring.Springweb.DTO.PaymentResponse;

public interface PaymentService {
    PaymentResponse confirmPayment(PaymentRequest request);
}
