package com.spring.Springweb.Controller;

import java.security.Principal; // Import cần thiết để lấy thông tin người dùng
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.Springweb.DTO.OrderCreateRequest;
import com.spring.Springweb.DTO.OrderResponse;
import com.spring.Springweb.Entity.Invoice; 
import com.spring.Springweb.Entity.User; 
import com.spring.Springweb.Repository.InvoiceRepository;
import com.spring.Springweb.Repository.UserRepository; // 👈 Cần thiết để tìm User ID
import com.spring.Springweb.Service.OrderService;
import com.spring.Springweb.Service.VNPayService;

import jakarta.servlet.http.HttpServletRequest; // Cần thiết để lấy địa chỉ IP
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private InvoiceRepository invoiceRepository; 
    @Autowired private VNPayService vnPayService; 
    
    // ✅ THÊM Repository User và JwtUtil (Nếu bạn dùng JwtUtil để xác thực)
    @Autowired private UserRepository userRepository;
    // @Autowired private JwtUtil jwtUtil; // Giữ lại nếu bạn cần giải mã token thủ công

  
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestBody OrderCreateRequest request,
            HttpServletRequest httpRequest,
            Principal principal) { // 👈 SỬ DỤNG PRINCIPAL

        if (principal == null) {
            // Lỗi 401 được xử lý bởi Security Filter, nhưng đây là lớp bảo vệ thứ hai
            return new ResponseEntity<>(Map.of("message", "Authentication required."), HttpStatus.UNAUTHORIZED);
        }
        
        // 1. Lấy username (email/phone) và tìm User Entity
        String username = principal.getName(); 
        User customer = userRepository.findByUsername(username) // Giả định findByUsername tìm bằng email/phone
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        Integer customerId = customer.getId();

        // 2. Gọi Service tạo Order/Invoice mới
        OrderResponse orderResponse = orderService.createOrder(request, customerId);
        
        // 3. Xử lý phản hồi tùy thuộc vào phương thức thanh toán
        String paymentMethod = orderResponse.getPaymentMethod().toLowerCase();
        Map<String, Object> response = new HashMap<>();

        if (paymentMethod.contains("cod") || paymentMethod.contains("receive")) {
            // Thanh toán khi nhận hàng (COD)
            response.put("message", "Order placed successfully (COD).");
            response.put("order", orderResponse);
            return ResponseEntity.ok(response);
            
        } else if (orderResponse.getStatus().equalsIgnoreCase("PENDING")) {
            // Thanh toán qua cổng (VNPay/Card) -> Cần tạo URL
            try {
                // Lấy Invoice Entity vừa tạo từ DB (dùng TxnRef)
                Invoice invoice = invoiceRepository.findByTxnRef(orderResponse.getTxnRef())
                        .orElseThrow(() -> new RuntimeException("Invoice not found in repository."));
                
                String ipAddress = httpRequest.getRemoteAddr();
                String paymentUrl = vnPayService.createPaymentUrl(invoice, ipAddress); 
                
                response.put("message", "Payment URL generated.");
                response.put("order", orderResponse);
                response.put("paymentUrl", paymentUrl); // 👈 Trả về URL cho frontend
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                // Lỗi này sẽ được bắt bởi GlobalExceptionHandler (500/BAD_REQUEST)
                throw new RuntimeException("Failed to generate payment URL: " + e.getMessage());
            }
        }
        
        // Phản hồi mặc định 
        response.put("order", orderResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'STAFF')")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(Principal principal) {
        
        // Principal luôn có khi @PreAuthorize('isAuthenticated') hoặc role nào đó
        String username = principal.getName();
        
        // 1. Lấy Customer Entity và ID
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        Integer customerId = customer.getId();

        // 2. Gọi Service để lấy đơn hàng theo Customer ID (Chỉ trả về các Order Online)
        List<OrderResponse> orders = orderService.getOrdersByCustomer(customerId);
        
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/all") 
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')") // 👈 Phân quyền chỉ cho Staff/Admin
    public ResponseEntity<List<OrderResponse>> getAllOrdersForAdmin() {
        
        // Gọi Service để lấy TẤT CẢ Order Online (AppointmentId = NULL)
        List<OrderResponse> allOrders = orderService.getAllOrders(); 
        
        return ResponseEntity.ok(allOrders);
    }
    
    @GetMapping("/{txnRef}")
    public ResponseEntity<OrderResponse> getOrderDetailByTxnRef(
            @PathVariable String txnRef,
            Principal principal) {
        
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        String username = principal.getName();
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        Integer customerId = customer.getId();

        // Gọi Service để lấy chi tiết đơn hàng (Service đã kiểm tra quyền sở hữu)
        OrderResponse orderDetail = orderService.getOrderByTxnRefAndCustomer(txnRef, customerId);
        
        return ResponseEntity.ok(orderDetail);
    }
}