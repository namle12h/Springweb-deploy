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

import com.spring.Springweb.DTO.InvoiceCreateRequest;
import com.spring.Springweb.DTO.InvoiceItemRequest;
import com.spring.Springweb.DTO.InvoiceItemResponse;
import com.spring.Springweb.DTO.InvoiceResponse;
import com.spring.Springweb.Entity.Appointment;
import com.spring.Springweb.Entity.Customer;
import com.spring.Springweb.Entity.Invoice;
import com.spring.Springweb.Entity.InvoiceItem;
import com.spring.Springweb.Entity.Product;
import com.spring.Springweb.Entity.ServiceEntity;
import com.spring.Springweb.Entity.Staff;
import com.spring.Springweb.Entity.User;
import com.spring.Springweb.Repository.AppointmentRepository;
import com.spring.Springweb.Repository.InvoiceItemRepository;
import com.spring.Springweb.Repository.InvoiceRepository;
import com.spring.Springweb.Repository.ProductRepository;
import com.spring.Springweb.Repository.ServiceRepository;
import com.spring.Springweb.Repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private InvoiceItemRepository invoiceItemRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private VNPayService vnpayService;

    @Override
    public InvoiceResponse createInvoice(InvoiceCreateRequest request) {
        // 🔹 Lấy Appointment và Customer
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        User user = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!(user instanceof Customer)) {
            throw new RuntimeException("User is not a customer!");
        }

        Customer customer = (Customer) user;

        // ============================
        // 🔍 Xử lý hóa đơn cũ
        // ============================
        Invoice invoice;
        Invoice existingInvoice = appointment.getInvoice();

        if (existingInvoice != null) {
            String status = existingInvoice.getStatus() != null
                    ? existingInvoice.getStatus().toUpperCase()
                    : "";

            switch (status) {
                case "PAID":
                    throw new RuntimeException("PAID:" + existingInvoice.getTxnRef());

                case "PENDING":
                case "CANCELED":
                    // ✅ Cho phép thanh toán lại, tái sử dụng hóa đơn cũ
                    String newTxnRef = "INV" + LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));
                    existingInvoice.setTxnRef(newTxnRef);
                    existingInvoice.setStatus("PENDING");
                    existingInvoice.setUpdatedAt(LocalDateTime.now());
                    existingInvoice.setExpiredAt(LocalDateTime.now().plusMinutes(30));
                    existingInvoice.setNotes(request.getNotes());
                    invoice = existingInvoice;
                    break;

                default:
                    throw new RuntimeException("INVALID_STATUS:" + status);
            }
        } else {
            // ✅ Chưa có hóa đơn → tạo mới
            invoice = new Invoice();
            invoice.setAppointment(appointment);
            appointment.setInvoice(invoice);
            invoice.setCreatedAt(LocalDateTime.now());
            invoice.setUpdatedAt(LocalDateTime.now());
            invoice.setStatus("PENDING");
            String txnRef = "INV" + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));
            invoice.setTxnRef(txnRef);
            invoice.setExpiredAt(LocalDateTime.now().plusMinutes(30));
        }

        // ============================
        // 🔹 Tính toán tổng tiền
        // ============================
        BigDecimal subTotal = BigDecimal.ZERO;
        List<InvoiceItem> invoiceItems = new ArrayList<>();

        for (InvoiceItemRequest itemReq : request.getItems()) {
            BigDecimal lineTotal = itemReq.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subTotal = subTotal.add(lineTotal);

            InvoiceItem item = new InvoiceItem();
            item.setQty(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());

            if (itemReq.getServiceId() != null) {
                ServiceEntity svc = serviceRepository.findById(itemReq.getServiceId())
                        .orElseThrow(() -> new RuntimeException("Service not found"));
                item.setService(svc);
            }

            if (itemReq.getProductId() != null) {
                Product prod = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                item.setProduct(prod);
            }

            item.setInvoice(invoice);
            invoiceItems.add(item);
        }

        // ============================
        // 🔹 Tính VAT và Tổng cộng
        // ============================
        BigDecimal vatPercent = request.getVat() != null
                ? request.getVat()
                : BigDecimal.ZERO;

        BigDecimal vatAmount = subTotal.multiply(vatPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal discount = request.getDiscountAmount() != null
                ? request.getDiscountAmount()
                : BigDecimal.ZERO;

        BigDecimal total = subTotal.add(vatAmount).subtract(discount);

        // ============================
        // 🔹 Gán dữ liệu vào hóa đơn
        // ============================
        invoice.setCustomer(customer);
        invoice.setSubTotal(subTotal);
        invoice.setVat(vatPercent);
        invoice.setDiscountAmount(discount);
        invoice.setTotal(total);
        invoice.setPaymentMethod(request.getPaymentMethod());
        invoice.setAmountPaid(request.getAmountPaid());
        invoice.setNotes(request.getNotes());

        // ============================
        // 🔹 Xử lý trạng thái thanh toán
        // ============================
        String method = request.getPaymentMethod() != null
                ? request.getPaymentMethod().toLowerCase()
                : "cash";

        switch (method) {
            case "card":
            case "qr":
            case "wallet":
            case "bank":
                invoice.setStatus("PENDING");
                invoice.setExpiredAt(LocalDateTime.now().plusMinutes(30));
                break;
            case "cash":
                invoice.setStatus("PAID");
                break;
            default:
                invoice.setStatus("UNPAID");
                break;
        }

        // 🔹 Tính tiền thừa nếu có
        if (request.getAmountPaid() != null) {
            BigDecimal change = request.getAmountPaid().subtract(total);
            invoice.setChangeAmount(change);
        }

        // ============================
        // 🔹 Lưu dữ liệu
        // ============================
        invoiceRepository.save(invoice);
        invoiceItemRepository.saveAll(invoiceItems);

        // ✅ Nếu thanh toán ngay (cash) thì cập nhật Appointment
        if ("PAID".equalsIgnoreCase(invoice.getStatus())) {
            appointment.setStatus("PAID");
            BigDecimal totalMoney = invoice.getTotal();

            Staff staffUser = appointment.getStaff();
            if (staffUser != null) {
                // Lấy totalRevenue cũ (giả sử Staff cũng là User, cần xử lý Entity Staff nếu có)
                // Dữ liệu ban đầu bạn gửi có vẻ Staff được lưu trong User.
                // Tùy thuộc vào Entity Staff/User của bạn, đây là cách cập nhật an toàn nhất:

                // Giả định đối tượng User có trường totalRevenue
                BigDecimal staffRevenue = staffUser.getTotalRevenue() != null
                        ? staffUser.getTotalRevenue()
                        : BigDecimal.ZERO;

                staffUser.setTotalRevenue(staffRevenue.add(totalMoney));
                userRepository.save(staffUser); // Lưu lại Staff
            }
            int earnedPoints = totalMoney.divide(new BigDecimal("10000"), RoundingMode.DOWN).intValue();

            if (earnedPoints > 0) {
                Integer oldPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
                customer.setLoyaltyPoints(oldPoints + earnedPoints);

                // ✅ CẬP NHẬT RANK THEO ĐIỂM
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

                BigDecimal previousTotal = customer.getTotalSpent() != null
                        ? customer.getTotalSpent()
                        : BigDecimal.ZERO;

                customer.setTotalSpent(previousTotal.add(invoice.getTotal()));
                userRepository.save(customer); // ✅ Lưu customer

                // ==========================================
//                int earnedPoints = totalMoney.divide(new BigDecimal("10000"), RoundingMode.DOWN).intValue();
            }

        } else if ("PENDING".equalsIgnoreCase(invoice.getStatus())) {
            appointment.setStatus("PendingPayment");
        }
        appointmentRepository.save(appointment);

        // ============================
        // 🔹 Tạo phản hồi trả về
        // ============================
        List<InvoiceItemResponse> itemResponses = invoiceItems.stream().map(item
                -> InvoiceItemResponse.builder()
                        .id(item.getId())
                        .serviceId(item.getService() != null ? item.getService().getId() : null)
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .qty(item.getQty())
                        .unitPrice(item.getUnitPrice())
                        .lineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQty())))
                        .build()
        ).toList();

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .customerId(customer.getId())
                .txnRef(invoice.getTxnRef())
                .appointmentId(appointment.getId())
                .subTotal(subTotal)
                .vat(vatPercent)
                .discountAmount(discount)
                .total(total)
                .status(invoice.getStatus())
                .paymentMethod(invoice.getPaymentMethod())
                .amountPaid(invoice.getAmountPaid())
                .changeAmount(invoice.getChangeAmount())
                .createdAt(invoice.getCreatedAt())
                .items(itemResponses)
                .build();
    }

    @Override
    public List<InvoiceResponse> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll();
        return invoices.stream().map(this::toResponse).toList();
    }

    @Override
    public Page<InvoiceResponse> getAllInvoices(int page, int size, String sortBy) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy).descending());

        Page<Invoice> invoicePage = invoiceRepository.findAll(pageRequest);

        return invoicePage.map(this::toResponse);
    }

    @Override
    public List<InvoiceResponse> getInvoicesByCustomer(Integer customerId) {
        List<Invoice> invoices = invoiceRepository.findByCustomer_Id(customerId);
        return invoices.stream().map(this::toResponse).toList();
    }

    @Override
    public List<InvoiceResponse> getInvoicesByStaff(Integer staffId) {
        List<Invoice> invoices = invoiceRepository.findByAppointment_Staff_Id(staffId);
        return invoices.stream().map(this::toResponse).toList();
    }

    // 🔹 Helper chuyển entity -> response
    private InvoiceResponse toResponse(Invoice inv) {
        List<InvoiceItemResponse> itemResponses = new ArrayList<>();

        if (inv.getItems() != null) {
            for (InvoiceItem item : inv.getItems()) {
                InvoiceItemResponse res = InvoiceItemResponse.builder()
                        .id(item.getId())
                        .serviceId(item.getService() != null ? item.getService().getId() : null)
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .qty(item.getQty())
                        .unitPrice(item.getUnitPrice())
                        .lineTotal(item.getUnitPrice()
                                .multiply(BigDecimal.valueOf(item.getQty())))
                        .build();
                itemResponses.add(res);
            }
        }

        return InvoiceResponse.builder()
                .id(inv.getId())
                .customerId(inv.getCustomer().getId())
                .appointmentId(inv.getAppointment() != null ? inv.getAppointment().getId() : null)
                .subTotal(inv.getSubTotal())
                .vat(inv.getVat())
                .discountAmount(inv.getDiscountAmount())
                .total(inv.getTotal())
                .status(inv.getStatus())
                .createdAt(inv.getCreatedAt())
                .paymentMethod(inv.getPaymentMethod())
                .amountPaid(inv.getAmountPaid())
                .changeAmount(inv.getChangeAmount())
                .notes(inv.getNotes())
                .items(itemResponses)
                .txnRef(inv.getTxnRef())
                .build();
    }

    private Invoice handleExistingInvoice(Appointment appointment) {
        Invoice existing = appointment.getInvoice();
        if (existing == null) {
            return null;
        }

        switch (existing.getStatus().toUpperCase()) {
            case "PENDING":
                // Nếu còn hạn -> vẫn đang chờ thanh toán
                if (existing.getExpiredAt() != null && existing.getExpiredAt().isAfter(LocalDateTime.now())) {
                    // Trả về để frontend hiển thị lại màn QR / retry thanh toán
                    return existing;
                }
                // Hết hạn -> đánh dấu EXPIRED và cho phép tạo mới
                existing.setStatus("EXPIRED");
                invoiceRepository.save(existing);
                return null;

            case "PAID":
                // Không cho tạo lại hóa đơn nếu đã thanh toán thành công
                throw new RuntimeException("PAID:" + existing.getTxnRef());

            case "CANCELLED":
            case "EXPIRED":
                // Cho phép tạo hóa đơn mới
                return null;

            default:
                return null;
        }
    }

    @Override
    public InvoiceResponse getInvoiceByTxnRef(String txnRef) {
        Invoice invoice = invoiceRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        InvoiceResponse response = toResponse(invoice);

        if (!"PAID".equalsIgnoreCase(invoice.getStatus())) {
            // Lấy IP giả định hoặc trống, vì không có HttpServletRequest ở đây
            String ipAddress = "127.0.0.1";
            String paymentUrl = vnpayService.createPaymentUrl(invoice, ipAddress);
            response.setPaymentUrl(paymentUrl);
        }

        return response;
    }
}
