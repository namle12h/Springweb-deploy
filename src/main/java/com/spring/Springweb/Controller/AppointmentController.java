package com.spring.Springweb.Controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.Springweb.DTO.AppointmentRequest;
import com.spring.Springweb.DTO.AppointmentResponse;
import com.spring.Springweb.Entity.Appointment;
import com.spring.Springweb.Service.AppointmentService;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> getAllAppointments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Page<AppointmentResponse> result = appointmentService.getAllAppointments(page, limit);
        return ResponseEntity.ok(result);
    }

//     @GetMapping("/{id}")
//     public ResponseEntity<Appointment> getAppointmentById(@PathVariable Integer id) {
//         return appointmentService.getAppointmentbyId(id)
//                 .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Integer id) {
        return ResponseEntity.ok(appointmentService.getAppointmentbyId(id));
    }


    @PostMapping("/public")
    public ResponseEntity<?> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        try {
            AppointmentResponse created = appointmentService.createAppointment(request);
            return ResponseEntity.ok(created);

        } catch (DataIntegrityViolationException ex) {
            String message = "Email đã tồn tại trong hệ thống. Vui lòng sử dụng email khác hoặc đăng nhập.";
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "status", 409,
                    "message", message
            ));

        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", ex.getMessage()
            ));

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", 500,
                    "message", "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau."
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable Integer id,
            @RequestBody Appointment appointmentDetails) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, appointmentDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Integer id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

}
