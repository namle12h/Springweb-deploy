/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.Springweb.DTO.PaymentRequest;
import com.spring.Springweb.DTO.PaymentResponse;
import com.spring.Springweb.Entity.Invoice;
import com.spring.Springweb.Entity.Payment;
import com.spring.Springweb.Repository.InvoiceRepository;
import com.spring.Springweb.Repository.PaymentRepository;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Override
    public PaymentResponse confirmPayment(PaymentRequest request) {
        // 1️⃣ Kiểm tra hóa đơn tồn tại
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // 2️⃣ Kiểm tra trạng thái hóa đơn
        if (!invoice.getStatus().equalsIgnoreCase("UNPAID")) {
            throw new RuntimeException("Invoice already paid or canceled");
        }

        // 3️⃣ Kiểm tra số tiền hợp lệ
        if (invoice.getTotal().compareTo(request.getAmount()) != 0) {
            throw new RuntimeException("Invalid payment amount");
        }

        // 4️⃣ Tạo bản ghi thanh toán
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setMethod(request.getMethod());
        payment.setAmount(request.getAmount());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 5️⃣ Cập nhật trạng thái hóa đơn
        invoice.setStatus("PAID");
        invoiceRepository.save(invoice);

        // 6️⃣ Trả phản hồi
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .message("Thanh toán thành công")
                .paidAt(payment.getPaidAt())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .build();
    }
}
