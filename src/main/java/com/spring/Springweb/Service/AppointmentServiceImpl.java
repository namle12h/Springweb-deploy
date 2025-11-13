/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.Springweb.DTO.AppointmentRequest;
import com.spring.Springweb.DTO.AppointmentResponse;
import com.spring.Springweb.DTO.NotificationDTO;
import com.spring.Springweb.Entity.Appointment;
import com.spring.Springweb.Entity.Customer;
import com.spring.Springweb.Entity.Room;
import com.spring.Springweb.Entity.ServiceEntity;
import com.spring.Springweb.Entity.Staff;
import com.spring.Springweb.Entity.User;
import com.spring.Springweb.Repository.AppointmentRepository;
import com.spring.Springweb.Repository.RoomRepository;
import com.spring.Springweb.Repository.ServiceRepository;
import com.spring.Springweb.Repository.StaffRepository;
import com.spring.Springweb.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    private final AuditLogService auditLogService;

    private final ServiceRepository serviceRepository;
    private final RoomRepository roomRepository;

    private final PasswordEncoder passwordEncoder;
    private final StaffRepository staffRepository;

    private final NotificationService notificationService;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest req) {
        Customer customer;

        // Nếu có customerId thì lấy trực tiếp từ DB
        if (req.getCustomerId() != null) {
            customer = (Customer) userRepository.findById(req.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id " + req.getCustomerId()));
        } else {
            // Nếu chưa login thì xử lý guest
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isLoggedIn = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());

            if (isLoggedIn) {
                Object principal = auth.getPrincipal();
                String email = (principal instanceof org.springframework.security.core.userdetails.UserDetails)
                        ? ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername()
                        : ((User) principal).getEmail();

                customer = (Customer) userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Logged in user not found in DB"));
            } else {
                // Guest booking logic
                if (userRepository.findByEmail(req.getEmail()).isPresent()
                        || userRepository.findByPhone(req.getPhone()).isPresent()) {
                    throw new RuntimeException("Email hoặc số điện thoại đã tồn tại, vui lòng nhập lại");
                }

                Customer newCust = new Customer();
                newCust.setName(req.getName());
                newCust.setPhone(req.getPhone());
                newCust.setEmail(req.getEmail());
                newCust.setPasswordHash(passwordEncoder.encode(req.getPhone()));
                newCust.setUsername(req.getEmail());
                newCust.setNotes("Guest booking");
                newCust.setCreatedAt(LocalDateTime.now());
                newCust.setRole("CUSTOMER");
                customer = userRepository.save(newCust);
            }
        }

        // Xử lý thời gian
        LocalDateTime startAt = LocalDateTime.parse(req.getDate() + "T" + req.getTime());
        LocalDateTime endAt = startAt.plusMinutes(60);

        // Lấy dịch vụ
        ServiceEntity service = serviceRepository.findById(req.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found: " + req.getServiceId()));

        // 5. Check duplicate từ DB
        long existing = appointmentRepository.countByCustomerAndServiceAndStartAtBetween(
                customer, service,
                startAt.minusHours(1),
                startAt.plusHours(1)
        );

        if (existing > 0) {
            throw new RuntimeException("Bạn chỉ được đặt 1 lần dịch vụ này trong vòng 1 giờ.");
        }

        // Tạo lịch hẹn
        Appointment appt = new Appointment();
        appt.setCustomer(customer);
        appt.setService(service);
        appt.setStartAt(startAt);
        appt.setEndAt(endAt);
        appt.setNotes(req.getNotes());
        appt.setStatus("Pending");
        appt.setContactEmail(req.getEmail());
        appt.setContactName(req.getName());
        appt.setContactPhone(req.getPhone());
        appt.setCreatedAt(LocalDateTime.now());

        appointmentRepository.save(appt);

        try {
            NotificationDTO noti = NotificationDTO.builder()
                    .title("Đặt lịch thành công 🎉")
                    .message("Cảm ơn " + customer.getName()
                            + ", bạn đã đặt lịch thành công cho ngày "
                            + req.getDate() + " lúc " + req.getTime() + ".")
                    .type("CUSTOMER")
                    .entityType("Appointment")
                    .entityId(appt.getId().longValue())
                    .targetId(customer != null ? customer.getId().longValue() : null)
                    .build();

            System.out.println("🔥 Creating notification for userId: " + noti.getTargetId());
            notificationService.createNotification(noti);
            System.out.println("✅ Notification created successfully!");

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo thông báo: " + e.getMessage());
        }

        auditLogService.logCreate("Appointment", appt.getId().longValue(), customer.getId().longValue());

        return mapToResponse(appt);
    }

    private AppointmentResponse mapToResponse(Appointment a) {
        AppointmentResponse res = new AppointmentResponse();
        res.setId(a.getId());
        res.setContactName(a.getContactName());
        res.setContactEmail(a.getContactEmail());
        res.setContactPhone(a.getContactPhone());
        res.setStatus(a.getStatus());
        res.setNotes(a.getNotes());
        res.setStartAt(a.getStartAt());
        res.setEndAt(a.getEndAt());
        res.setServiceName(a.getService() != null ? a.getService().getName() : null);
        res.setStaffName(a.getStaff() != null ? a.getStaff().getName() : null);
        res.setRoomName(a.getRoom() != null ? a.getRoom().getName() : null);

        if (a.getCustomer() != null) {
            res.setCustomerId(a.getCustomer().getId());
            res.setCustomer(a.getCustomer());
        }

        // ✅ Staff
        if (a.getStaff() != null) {
            res.setStaffId(a.getStaff().getId());
            res.setStaffName(a.getStaff().getName());
        } else {
            res.setStaffId(null);
            res.setStaffName(null);
        }

        // ✅ Room
        if (a.getRoom() != null) {
            res.setRoomId(a.getRoom().getId());
            res.setRoomName(a.getRoom().getName());
        } else {
            res.setRoomId(null);
            res.setRoomName(null);
        }

        if (a.getService() != null) {
            res.setService(a.getService()); // ✅ truyền nguyên entity vào DTO
            res.setServiceName(a.getService().getName());
        } else {
            res.setService(null);
            res.setServiceName(null);
        }

        // ✅ Thêm thông tin hóa đơn (Invoice)
        if (a.getInvoice() != null) {
            res.setInvoiceId(a.getInvoice().getId());
            res.setInvoiceStatus(a.getInvoice().getStatus());
        } else {
            res.setInvoiceStatus("UNPAID");
        }

        if (a.getInvoice() != null) {
            res.setInvoiceId(a.getInvoice().getId());
            res.setInvoiceStatus(a.getInvoice().getStatus());
            res.setInvoiceTxnRef(a.getInvoice().getTxnRef());
        }

        return res;
    }

    // @Override
    // public Optional<Appointment> getAppointmentbyId(Integer customerId) {
    //     return appointmentRepository.findById(customerId);
    // }
    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointment(Integer id, Appointment appointmentDetails) {
        Appointment existing = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Long performedBy = existing.getCustomer() != null
                ? existing.getCustomer().getId().longValue()
                : null;

        // ✅ Update StartAt
        if (appointmentDetails.getStartAt() != null && !appointmentDetails.getStartAt().equals(existing.getStartAt())) {
            auditLogService.logUpdate("Appointment", id.longValue(), "StartAt",
                    String.valueOf(existing.getStartAt()), String.valueOf(appointmentDetails.getStartAt()), performedBy);
            existing.setStartAt(appointmentDetails.getStartAt());
        }

        // ✅ Update EndAt
        if (appointmentDetails.getEndAt() != null && !appointmentDetails.getEndAt().equals(existing.getEndAt())) {
            auditLogService.logUpdate("Appointment", id.longValue(), "EndAt",
                    String.valueOf(existing.getEndAt()), String.valueOf(appointmentDetails.getEndAt()), performedBy);
            existing.setEndAt(appointmentDetails.getEndAt());
        }

        // ✅ Update Status
        if (appointmentDetails.getStatus() != null && !appointmentDetails.getStatus().equals(existing.getStatus())) {
            auditLogService.logUpdate("Appointment", id.longValue(), "Status",
                    existing.getStatus(), appointmentDetails.getStatus(), performedBy);
            existing.setStatus(appointmentDetails.getStatus());
        }

        // ✅ Update Notes
        if (appointmentDetails.getNotes() != null && !appointmentDetails.getNotes().equals(existing.getNotes())) {
            auditLogService.logUpdate("Appointment", id.longValue(), "Notes",
                    existing.getNotes(), appointmentDetails.getNotes(), performedBy);
            existing.setNotes(appointmentDetails.getNotes());
        }

        // ✅ Update Service
        if (appointmentDetails.getServiceId() != null) {
            ServiceEntity service = serviceRepository.findById(appointmentDetails.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Service not found with id " + appointmentDetails.getServiceId()));
            existing.setService(service);
        }

        // ✅ Update Staff
        Integer staffId = appointmentDetails.getStaff() != null && appointmentDetails.getStaff().getId() != null
                ? appointmentDetails.getStaff().getId()
                : appointmentDetails.getStaffId();

        if (staffId != null) {
            Staff staff = staffRepository.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Staff not found with id " + staffId));
            existing.setStaff(staff);
        }

        // ✅ Update Room
        Integer roomId = appointmentDetails.getRoom() != null && appointmentDetails.getRoom().getId() != null
                ? appointmentDetails.getRoom().getId()
                : appointmentDetails.getRoomId();

        if (roomId != null) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id " + roomId));
            existing.setRoom(room);
        }

        // ✅ Update Contact info
        if (appointmentDetails.getContactName() != null && !appointmentDetails.getContactName().equals(existing.getContactName())) {
            auditLogService.logUpdate("Appointment", id.longValue(), "ContactName",
                    existing.getContactName(), appointmentDetails.getContactName(), performedBy);
            existing.setContactName(appointmentDetails.getContactName());
        }

        if (appointmentDetails.getContactEmail() != null && !appointmentDetails.getContactEmail().equals(existing.getContactEmail())) {
            auditLogService.logUpdate("Appointment", id.longValue(), "ContactEmail",
                    existing.getContactEmail(), appointmentDetails.getContactEmail(), performedBy);
            existing.setContactEmail(appointmentDetails.getContactEmail());
        }

        if (appointmentDetails.getContactPhone() != null && !appointmentDetails.getContactPhone().equals(existing.getContactPhone())) {
            auditLogService.logUpdate("Appointment", id.longValue(), "ContactPhone",
                    existing.getContactPhone(), appointmentDetails.getContactPhone(), performedBy);
            existing.setContactPhone(appointmentDetails.getContactPhone());
        }

        Appointment saved = appointmentRepository.save(existing);
        // ✅ Tạo thông báo sau khi cập nhật
        try {
            if (existing.getCustomer() != null) {
                NotificationDTO noti = NotificationDTO.builder()
                        .title("Cập nhật dịch vụ 🎉")
                        .message("Xin chào " + existing.getCustomer().getName()
                                + ", hệ thống đã cập nhật thông tin lịch hẹn của bạn. Vui lòng kiểm tra chi tiết!")
                        .type("CUSTOMER")
                        .entityType("Appointment")
                        .entityId(existing.getId().longValue())
                        .targetId(existing.getCustomer().getId().longValue())
                        .build();

                System.out.println("🔥 Creating notification for userId: " + noti.getTargetId());
                notificationService.createNotification(noti);
                System.out.println("✅ Notification created successfully!");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo thông báo: " + e.getMessage());
            e.printStackTrace();
        }

        return mapToResponse(saved);
    }

    @Override
    public void deleteAppointment(Integer id) {
        appointmentRepository.deleteById(id);
    }

    @Override
    public Page<AppointmentResponse> getAllAppointments(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "CreatedAt"));
        Page<Appointment> appts = appointmentRepository.findAll(pageable);

        // map từng Appointment -> AppointmentResponse
        return appts.map(this::mapToResponse);
    }

    @Override
    public AppointmentResponse getAppointmentbyId(Integer id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));

        return mapToResponse(appointment);
    }

    @Override
    public List<AppointmentResponse> getAppointmentsByCustomer(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        List<Appointment> appointments = appointmentRepository.findByCustomerOrderByCreatedAtDesc(user);

        return appointments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public AppointmentResponse cancelAppointment(Integer id, User currentUser) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        // 🔹 Kiểm tra quyền sở hữu (chỉ chủ sở hữu mới được hủy)
        if (appointment.getCustomer() == null
                || !appointment.getCustomer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền hủy lịch này!");
        }

        // 🔹 Kiểm tra trạng thái
        if ("Cancelled".equalsIgnoreCase(appointment.getStatus())) {
            throw new RuntimeException("Lịch hẹn này đã bị hủy trước đó!");
        }

        if ("Completed".equalsIgnoreCase(appointment.getStatus())) {
            throw new RuntimeException("Lịch hẹn đã hoàn thành, không thể hủy!");
        }

        // 🔹 Cập nhật trạng thái
        appointment.setStatus("Cancelled");
        appointment.setNotes("Khách hàng tự hủy lịch hẹn");
        appointment.setUpdatedAt(LocalDateTime.now());

        appointmentRepository.save(appointment);

        auditLogService.logUpdate("Appointment", id.longValue(), "Status",
                "Pending", "Cancelled", currentUser.getId().longValue());

        return mapToResponse(appointment);
    }

}
