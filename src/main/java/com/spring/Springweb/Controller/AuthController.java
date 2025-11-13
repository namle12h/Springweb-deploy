package com.spring.Springweb.Controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.Springweb.DTO.AppointmentResponse;
import com.spring.Springweb.DTO.UpdateProfileDTO;
import com.spring.Springweb.Entity.RefreshToken;
import com.spring.Springweb.Entity.User;
import com.spring.Springweb.Repository.UserRepository;
import com.spring.Springweb.Service.AppointmentService;
import com.spring.Springweb.Service.RefreshTokenService;
import com.spring.Springweb.Service.UserService;
import com.spring.Springweb.util.JwtUtil;

import jakarta.persistence.DiscriminatorValue;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    // 🟢 Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sai email hoặc mật khẩu"));
        }

        // 🔸 Sinh Access Token
        String accessToken = jwtUtil.generateToken(
                Map.of("role", user.getRole()),
                user.getUsername()
        );

        // 🔸 Sinh Refresh Token và lưu DB
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken()
        ));
    }

    // 🟢 Lấy thông tin profile
    @GetMapping("/get-profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Thiếu token hoặc sai định dạng"));
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Token trống hoặc không hợp lệ"));
        }

        String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token không hợp lệ hoặc hết hạn"));
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("username", user.getUsername());
        userMap.put("name", user.getName());
        userMap.put("phone", user.getPhone());
        userMap.put("address", user.getAddress());
        userMap.put("dob", user.getDob());
        userMap.put("gender", user.getGender());
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("role", user.getClass().getAnnotation(DiscriminatorValue.class).value());

        return ResponseEntity.ok(Map.of(
                "user", userMap
        ));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(HttpServletRequest request, @RequestBody UpdateProfileDTO updatedUser) {
        try {
            User savedUser = userService.updateProfile(request, updatedUser);
            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật thông tin thành công",
                    "user", Map.of(
                            "id", savedUser.getId(),
                            "name", savedUser.getName(),
                            "email", savedUser.getEmail(),
                            "phone", savedUser.getPhone(),
                            "address", savedUser.getAddress(),
                            "dob", savedUser.getDob(),
                            "gender", savedUser.getGender(),
                            "role", savedUser.getRole()
                    )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 🟢 Đổi mật khẩu
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Thiếu token"));
        }

        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Token trống"));
        }

        String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token không hợp lệ"));
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu cũ không đúng"));
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu xác nhận không khớp"));
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công!"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String refreshTokenValue = body.get("refreshToken");

        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu refresh token"));
        }

        RefreshToken oldToken = refreshTokenService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"));

        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenService.deleteByEmail(oldToken.getEmail());
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token đã hết hạn, vui lòng đăng nhập lại"));
        }

        if (oldToken.getRefreshCount() >= 5) {
            refreshTokenService.deleteByEmail(oldToken.getEmail());
            return ResponseEntity.status(401).body(Map.of("error", "Số lần refresh đã đạt giới hạn, vui lòng đăng nhập lại"));
        }

        oldToken.setRefreshCount(oldToken.getRefreshCount() + 1);
        oldToken.setExpiryDate(Instant.now().plus(2, ChronoUnit.HOURS));  // Cập nhật thời gian hết hạn mới

        String newToken = UUID.randomUUID().toString();  // Tạo refresh token mới

        oldToken.setToken(newToken);  // Cập nhật lại token mới

        refreshTokenService.save(oldToken);

        User user = userRepository.findByEmail(oldToken.getEmail())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        String newAccessToken = jwtUtil.generateToken(
                Map.of("role", user.getRole()),
                user.getUsername()
        );

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newToken // Trả về refresh token mới
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Thiếu token"));
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Token trống"));
        }

        String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token không hợp lệ"));
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        refreshTokenService.deleteByEmail(user.getEmail());
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công!"));
    }

    // 🟢 Lấy danh sách lịch hẹn
    @GetMapping("/my-appointments")
    public ResponseEntity<?> getMyAppointments(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Thiếu token"));
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Token trống"));
        }

        String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token không hợp lệ"));
        }

        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByCustomer(username);
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/cancel-appointment/{id}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Integer id,
            HttpServletRequest request) {
        try {
            // 🔹 Lấy user hiện tại từ token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Thiếu token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // 🔹 Gọi service xử lý hủy
            AppointmentResponse res = appointmentService.cancelAppointment(id, user);

            return ResponseEntity.ok(Map.of(
                    "message", "Hủy lịch hẹn thành công!",
                    "appointment", res
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
}
