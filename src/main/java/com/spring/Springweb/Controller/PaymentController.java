package com.spring.Springweb.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.Springweb.Entity.Appointment;
import com.spring.Springweb.Entity.Invoice;
import com.spring.Springweb.Entity.Payment;
import com.spring.Springweb.Repository.AppointmentRepository;
import com.spring.Springweb.Repository.InvoiceRepository;
import com.spring.Springweb.Repository.PaymentRepository;
import com.spring.Springweb.Service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;

@RestController
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Value("${payment.frontend-return-url}")
    private String frontendReturnBase;

    // ✅ Tạo link thanh toán cho 1 hóa đơn
    @GetMapping("/create-payment")
    public String createPayment(@RequestParam("invoiceId") Integer invoiceId,
            HttpServletRequest request) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        String ipAddress = request.getRemoteAddr();
        String paymentUrl = vnPayService.createPaymentUrl(invoice, ipAddress);

        return "Redirect to: <a href=\"" + paymentUrl + "\">" + paymentUrl + "</a>";
    }

    @GetMapping("/payment-return")
    public void paymentReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String responseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");
        String amountParam = request.getParameter("vnp_Amount");
        String transactionNo = request.getParameter("vnp_TransactionNo");

        BigDecimal amount = new BigDecimal(amountParam).divide(BigDecimal.valueOf(100));

        Invoice invoice = invoiceRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new RuntimeException("Invoice not found for txnRef: " + txnRef));

        if ("00".equals(responseCode)) {
            // ✅ Cập nhật trạng thái hóa đơn
            invoice.setStatus("PAID");
            invoice.setUpdatedAt(LocalDateTime.now());
            if (transactionNo != null) {
                invoice.setTransactionId(transactionNo);
            }

            Payment payment = new Payment();
            payment.setInvoice(invoice);
            payment.setMethod("qr");
            payment.setAmount(amount);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // ✅ Cập nhật trạng thái Appointment tương ứng
            Appointment appointment = invoice.getAppointment();
            if (appointment != null) {
                appointment.setStatus("PAID"); // hoặc "Completed" nếu bạn dùng status này để thể hiện đã thanh toán
                appointmentRepository.save(appointment);
            }
            invoiceRepository.save(invoice);

            response.sendRedirect(
                    frontendReturnBase + "/payment-return"
                    + "?vnp_ResponseCode=" + responseCode
                    + "&vnp_TransactionStatus=" + responseCode
                    + "&vnp_TxnRef=" + txnRef
            );

        } else {
            invoice.setStatus("FAILED");
            invoiceRepository.save(invoice);
            response.sendRedirect(frontendReturnBase + responseCode + "&vnp_TxnRef=" + txnRef);
        }
    }
}
