/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.Springweb.DTO.NotificationDTO;
import com.spring.Springweb.DTO.OrderCreateRequest;
import com.spring.Springweb.DTO.OrderItemResponse;
import com.spring.Springweb.DTO.OrderResponse;
import com.spring.Springweb.DTO.ProductOrderItemRequest;
import com.spring.Springweb.Entity.Customer;
import com.spring.Springweb.Entity.Invoice;
import com.spring.Springweb.Entity.InvoiceItem;
import com.spring.Springweb.Entity.Product;
import com.spring.Springweb.Entity.User;
import com.spring.Springweb.Repository.InvoiceItemRepository;
import com.spring.Springweb.Repository.InvoiceRepository;
import com.spring.Springweb.Repository.ProductRepository;
import com.spring.Springweb.Repository.UserRepository;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private InvoiceItemRepository invoiceItemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private NotificationService notificationService;
    // @Autowired private AddressService addressService; // Giả định Service xử lý địa chỉ

    @Override
    public OrderResponse createOrder(OrderCreateRequest request, Integer customerId) {

        // 1. Lấy Customer
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!(user instanceof Customer)) {
            throw new RuntimeException("User is not a customer!");
        }

        Customer customer = (Customer) user;

        // 2. Tạo Invoice mới (Tái sử dụng Entity Invoice)
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());

        // ⚠️ RẤT QUAN TRỌNG: Thiết lập AppointmentId = NULL
        invoice.setAppointment(null);

        // Tạo TxnRef cho hóa đơn mới
        String txnRef = "ORDER" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));
        invoice.setTxnRef(txnRef);

        // 2. Kiểm tra Validation (Chỉ cần cho Online Order)
        if (request.getReceiverName() == null || request.getReceiverName().isBlank()
                || request.getAddressDetail() == null || request.getAddressDetail().isBlank()
                || request.getCityName() == null || request.getCityName().isBlank()) {

            // Ném ra lỗi Bad Request nếu thông tin giao hàng bị thiếu
            throw new IllegalArgumentException("Thông tin nhận hàng không được để trống.");
        }

        // 3. Gán thông tin địa chỉ vào Invoice
        invoice.setReceiverName(request.getReceiverName());
        invoice.setReceiverPhone(request.getReceiverPhone());
        invoice.setAddressDetail(request.getAddressDetail());
        invoice.setCityName(request.getCityName());
        invoice.setDistrictName(request.getDistrictName());
        invoice.setCommuneName(request.getCommuneName());
        invoice.setNotes(request.getNotes());
        // ============================
        // 3. TÍNH TOÁN VÀ LƯU CHI TIẾT ĐƠN HÀNG
        // ============================
        BigDecimal subTotal = BigDecimal.ZERO;
        List<InvoiceItem> invoiceItems = new ArrayList<>();

        for (ProductOrderItemRequest itemReq : request.getItems()) {
            BigDecimal price = itemReq.getPricePerUnit();
            if (price == null) {
                price = BigDecimal.ZERO;
            }

            BigDecimal lineTotal = price
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subTotal = subTotal.add(lineTotal);

            InvoiceItem item = new InvoiceItem();
            item.setQty(itemReq.getQuantity());
            item.setUnitPrice(price);
            item.setLineTotal(lineTotal);

            // 3a. Lấy và gán Product
            Product prod = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.getProductId()));
            item.setProduct(prod);

            // 3b. Giữ item.setService(null);
            item.setInvoice(invoice);
            invoiceItems.add(item);
        }

        // 4. Tính toán tổng cuối
        BigDecimal vatPercent = BigDecimal.ZERO; // Giả định VAT 0%
        BigDecimal discount = request.getDiscountAmount() != null
                ? request.getDiscountAmount()
                : BigDecimal.ZERO;
        BigDecimal shippingFee = request.getShippingFee() != null
                ? request.getShippingFee()
                : BigDecimal.ZERO;

        BigDecimal vatAmount = subTotal.multiply(vatPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Tổng tiền = SubTotal + VAT + Shipping - Discount
        BigDecimal total = subTotal.add(vatAmount).add(shippingFee).subtract(discount);

        // 5. Gán dữ liệu vào Invoice
        invoice.setSubTotal(subTotal);
        invoice.setVat(vatPercent);
        invoice.setDiscountAmount(discount);
        invoice.setTotal(total);
        invoice.setPaymentMethod(request.getPaymentMethod());
        // ⚠️ Thêm thông tin giao hàng vào Notes nếu cần, hoặc vào các cột khác của Invoice Entity
        // (Giả định Invoice Entity có thể lưu thông tin này hoặc bạn dùng một Order Entity riêng)

        // 6. Xử lý trạng thái thanh toán
        String method = request.getPaymentMethod().toLowerCase();
        if (method.contains("cod") || method.contains("receive")) {
            invoice.setStatus("UNPAID"); // Chờ thanh toán khi nhận hàng
        } else {
            invoice.setStatus("PENDING"); // Chờ thanh toán Online (VNPay/Card)
            invoice.setExpiredAt(LocalDateTime.now().plusMinutes(30));
        }

        invoice.setPaymentMethod(request.getPaymentMethod());

        // 7. Lưu dữ liệu
        invoiceRepository.save(invoice);
        invoiceItemRepository.saveAll(invoiceItems);

        if (invoice.getStatus().equals("PAID") || invoice.getStatus().equals("UNPAID")) { // Chỉ tính điểm cho đơn hàng thành công/chờ thanh toán
            
            // Lấy tổng tiền (Total) đã tính
            BigDecimal totalMoney = invoice.getTotal();
            // Công thức: 1 điểm cho mỗi 10,000₫ (hoặc công thức bạn đã định nghĩa)
            // RoundingMode.DOWN: Luôn làm tròn xuống (ví dụ: 19,999₫ -> 1 điểm)
            int earnedPoints = totalMoney.divide(new BigDecimal("10000"), RoundingMode.DOWN).intValue();

            if (earnedPoints > 0) {
                // Lấy điểm cũ
                Integer oldPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
                
                // 1. CẬP NHẬT ĐIỂM VÀ TỔNG CHI TIÊU
                customer.setLoyaltyPoints(oldPoints + earnedPoints);

                BigDecimal previousTotal = customer.getTotalSpent() != null
                        ? customer.getTotalSpent()
                        : BigDecimal.ZERO;

                customer.setTotalSpent(previousTotal.add(invoice.getTotal()));
                
                // 2. CẬP NHẬT RANK THEO ĐIỂM
                int totalPoints = customer.getLoyaltyPoints();

                String newRank;
                if (totalPoints >= 1500) {
                    newRank = "DIAMOND";
                } else if (totalPoints >= 600) {
                    newRank = "GOLD";
                } else if (totalPoints >= 200) {
                    newRank = "SILVER";
                } else {
                    newRank = "NEWBIE";
                }

                customer.setRankLevel(newRank);
                
                // 3. LƯU THÔNG TIN KHÁCH HÀNG ĐÃ CẬP NHẬT
                userRepository.save(customer);
                
                // Gửi thông báo về việc tích lũy điểm thưởng
                NotificationDTO pointNoti = NotificationDTO.builder()
                        .title("💰 Điểm thưởng đã cộng!")
                        .message("Bạn vừa tích lũy thành công " + earnedPoints + " điểm. Tổng điểm hiện tại: " + customer.getLoyaltyPoints() + ".")
                        .type("CUSTOMER")
                        .entityType("POINT")
                        .targetId(customer.getId().longValue())
                        .build();
                notificationService.createNotification(pointNoti);
            }
        }

        try {
            NotificationDTO noti = NotificationDTO.builder()
                    .title("🛍️ Đặt hàng thành công!")
                    .message("Cảm ơn " + customer.getName()
                            + ", đơn hàng " + invoice.getTxnRef()
                            + " của bạn đã được tạo thành công với tổng giá trị "
                            + total.toPlainString() + "₫.")
                    .type("CUSTOMER")
                    .entityType("ORDER")
                    .entityId(invoice.getId().longValue())
                    .targetId(customer.getId().longValue())
                    .build();

            System.out.println("📢 Creating notification for userId: " + noti.getTargetId());
            notificationService.createNotification(noti);
            System.out.println("✅ Notification created successfully!");
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo thông báo: " + e.getMessage());
        }

        // 8. Tạo phản hồi
        return OrderResponse.builder()
                .id(invoice.getId())
                .txnRef(invoice.getTxnRef())
                .total(total)
                .status(invoice.getStatus())
                .paymentMethod(invoice.getPaymentMethod())
                .message("Order created successfully.")
                .build();
    }

    // Lấy đơn hàng của khách hàng hiện tại
    @Override
    public List<OrderResponse> getOrdersByCustomer(Integer customerId) {
        // Lọc tất cả Invoices có CustomerId và AppointmentId = NULL
        List<Invoice> invoices = invoiceRepository.findByCustomer_Id(customerId);

        return invoices.stream()
                .filter(inv -> inv.getAppointment() == null) // Chỉ lấy Order Online
                .map(this::toOrderResponse)
                .toList();
    }

    private OrderResponse toOrderResponse(Invoice inv) {
        // 1. Khởi tạo List OrderItemResponse
        List<OrderItemResponse> items = List.of();

        // 2. Xác định loại đơn hàng và ánh xạ chi tiết
        if (inv.getAppointment() != null) {
            // ➡️ XỬ LÝ DỊCH VỤ/LỊCH HẸN (Giả định Entity/DTO đã tồn tại)
            // Nếu là Appointment, OrderItems sẽ là các dịch vụ (Services)
            items = inv.getItems().stream()
                    .filter(item -> item.getService() != null)
                    .map(item -> OrderItemResponse.builder()
                    // Cần ánh xạ từ Service Entity thay vì Product
                    .productId(null) // Không có Product ID
                    .productName(item.getService().getName())
                    .imageUrl(item.getService().getImageUrl()) // Giả định Service Entity có ImageUrl
                    .quantity(item.getQty())
                    .price(item.getUnitPrice())
                    .build()
                    ).toList();

        } else if (inv.getItems() != null && !inv.getItems().isEmpty() && inv.getItems().get(0).getProduct() != null) {
            // ➡️ XỬ LÝ SẢN PHẨM (Order Online - logic hiện tại)
            items = inv.getItems().stream()
                    .map(item -> OrderItemResponse.builder()
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .brand(item.getProduct().getBrand())
                    .imageUrl(item.getProduct().getImageUrl())
                    .quantity(item.getQty())
                    .price(item.getUnitPrice())
                    .build())
                    .toList();
        }

        // 3. Xác định Tên người nhận chính
        String primaryReceiverName = inv.getReceiverName() != null
                ? inv.getReceiverName() // Dùng ReceiverName nếu có (Order Online)
                : inv.getCustomer().getName(); // Nếu không có, dùng tên khách hàng (Appointment)

        // 4. Trả về phản hồi
        return OrderResponse.builder()
                .id(inv.getId())
                .txnRef(inv.getTxnRef())
                .status(inv.getStatus())
                .total(inv.getTotal())
                .paymentMethod(inv.getPaymentMethod())
                .message(inv.getNotes())
                // 🎯 Sử dụng tên đã xác định
                .receiverName(primaryReceiverName)
                .receiverPhone(inv.getReceiverPhone())
                .receiverAddress(inv.getAddressDetail() != null ? inv.getAddressDetail() + ", ..." : null)
                .orderItems(items)
                .build();
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        // ⚠️ LƯU Ý: Nếu Repository.findAll() không tải Lazy Collections,
        // bạn sẽ cần dùng JPQL JOIN FETCH ở Repository.
        List<Invoice> allInvoices = invoiceRepository.findAll();

        return allInvoices.stream()
                // ❌ BỎ FILTER NÀY ĐI
                // .filter(inv -> inv.getAppointment() == null) 
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    public OrderResponse getOrderByTxnRefAndCustomer(String txnRef, Integer customerId) {
        // 1. Tìm Invoice/Order theo TxnRef
        Invoice invoice = invoiceRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with txnRef: " + txnRef));

        // 2. Kiểm tra quyền sở hữu (Security check)
        if (!invoice.getCustomer().getId().equals(customerId)) {
            // Ném ra lỗi bảo mật nếu Customer ID không khớp
            throw new SecurityException("Access denied. You do not own this order.");
        }

        // 3. Kiểm tra Order phải là Online Order
        if (invoice.getAppointment() != null) {
            throw new IllegalArgumentException("Invalid order type.");
        }

        // 4. Trả về phản hồi chi tiết
        return this.toOrderResponse(invoice);
    }
    
    @Override
    @Transactional
    public Invoice processOrderPaymentSuccess(String txnRef, BigDecimal amountPaid) {
        
        Invoice invoice = invoiceRepository.findByTxnRef(txnRef).orElseThrow(() -> new RuntimeException("Invoice not found: " + txnRef));

        if (invoice.getStatus().equals("PAID")) {
            return invoice; // Đã thanh toán, bỏ qua
        }
        
        // 1. Cập nhật trạng thái và số tiền
        invoice.setStatus("PAID");
        invoice.setAmountPaid(amountPaid);
        invoice.setUpdatedAt(LocalDateTime.now());
        
        // 2. LẤY CUSTOMER VÀ TÍCH ĐIỂM
        User user = userRepository.findById(invoice.getCustomer().getId()).orElseThrow(() -> new RuntimeException("Customer not found"));
        Customer customer = (Customer) user;
        
        BigDecimal totalMoney = invoice.getTotal();
        int earnedPoints = totalMoney.divide(new BigDecimal("10000"), RoundingMode.DOWN).intValue();

        if (earnedPoints > 0) {
            
            Integer oldPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
            BigDecimal previousTotal = customer.getTotalSpent() != null ? customer.getTotalSpent() : BigDecimal.ZERO;
            
            // CẬP NHẬT ĐIỂM, TỔNG CHI TIÊU VÀ RANK
            customer.setLoyaltyPoints(oldPoints + earnedPoints);
            customer.setTotalSpent(previousTotal.add(invoice.getTotal()));
            
            int totalPoints = customer.getLoyaltyPoints();

            String newRank;
            if (totalPoints >= 1500) newRank = "DIAMOND";
            else if (totalPoints >= 600) newRank = "GOLD";
            else if (totalPoints >= 200) newRank = "SILVER";
            else newRank = "NEWBIE";

            customer.setRankLevel(newRank);
            
            userRepository.save(customer);

            // Gửi thông báo tích điểm
            NotificationDTO pointNoti = NotificationDTO.builder() /* ... */ .build();
            notificationService.createNotification(pointNoti);
        }
        
        // 3. Lưu Invoice đã cập nhật
        return invoiceRepository.save(invoice);
    }
}
