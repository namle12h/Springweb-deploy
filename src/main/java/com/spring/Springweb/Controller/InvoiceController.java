/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.Springweb.DTO.InvoiceCreateRequest;
import com.spring.Springweb.DTO.InvoiceResponse;
import com.spring.Springweb.Entity.Invoice;
import com.spring.Springweb.Repository.InvoiceRepository;
import com.spring.Springweb.Service.InvoiceService;
import com.spring.Springweb.Service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private VNPayService vnPayService;

    // ✅ Tạo hóa đơn mới
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> createInvoice(@RequestBody InvoiceCreateRequest request, HttpServletRequest httpRequest) {

        InvoiceResponse invoiceResponse = invoiceService.createInvoice(request);
        // 🔹 Nếu là QR thì gọi sang VNPay
        if ("qr".equalsIgnoreCase(request.getPaymentMethod())) {
            Invoice invoice = invoiceRepository.findById(invoiceResponse.getId())
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));
            String ipAddress = httpRequest.getRemoteAddr();
            String paymentUrl = vnPayService.createPaymentUrl(invoice, ipAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);
            response.put("invoiceId", invoice.getId());
            response.put("txnRef", invoice.getTxnRef());

            return ResponseEntity.ok(response);

        }
//        return ResponseEntity.ok(invoiceService.createInvoice(request));
        return ResponseEntity.ok(invoiceResponse);
    }

    // ✅ Lấy tất cả hóa đơn
//    @GetMapping
//    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
//    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
//        return ResponseEntity.ok(invoiceService.getAllInvoices());
//    }
    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> getInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok(invoiceService.getAllInvoices(page, size, sortBy));
    }

    // ✅ Lấy hóa đơn theo khách hàng
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByCustomer(@PathVariable Integer customerId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByCustomer(customerId));
    }

    // ✅ Lấy hóa đơn theo nhân viên
    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByStaff(@PathVariable Integer staffId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByStaff(staffId));
    }

    @GetMapping("/{txnRef}")
    public ResponseEntity<InvoiceResponse> getInvoiceByTxnRef(@PathVariable String txnRef) {
        InvoiceResponse response = invoiceService.getInvoiceByTxnRef(txnRef);
        return ResponseEntity.ok(response);
    }
}
