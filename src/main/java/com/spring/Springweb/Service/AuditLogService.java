
package com.spring.Springweb.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.Springweb.Entity.AuditLog;
import com.spring.Springweb.Entity.User;
import com.spring.Springweb.Repository.AuditLogRepository;
import com.spring.Springweb.Repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    // ✅ Ghi log an toàn (trong transaction)
    @Transactional
    public void log(String entity, Long entityId, String action,
            String field, String oldValue, String newValue, Long performedBy) {

        User performer = getCurrentUser();

        // Nếu không có user trong context nhưng có performedBy => lấy thủ công
        if (performer == null && performedBy != null) {
            performer = userRepository.findById(performedBy.intValue()).orElse(null);
        }

        AuditLog log = AuditLog.builder()
                .entity(entity)
                .entityId(entityId)
                .action(action)
                .field(field)
                .oldValue(oldValue)
                .newValue(newValue)
                .performedBy(performer != null ? performer.getId().longValue() : 0L)
                .performedByName(performer != null ? performer.getName() : "System")
                .role(performer != null ? performer.getRole() : "SYSTEM")
                .performedAt(LocalDateTime.now())
                .status("SUCCESS")
                .build();

        try {
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Ghi log lỗi nhẹ, không lưu 2 lần
            System.err.println("⚠️ Failed to save audit log: " + e.getMessage());
        }
    }

    // -----------------------------
    // 📒 Các hàm log tiện ích
    // -----------------------------
    public void logCreate(String entity, Long entityId, Long performedBy) {
        log(entity, entityId, "CREATE", null, null, null, performedBy);
    }

    public void logDelete(String entity, Long entityId, Long performedBy) {
        log(entity, entityId, "DELETE", null, null, null, performedBy);
    }

    public void logUpdate(String entity, Long entityId, String field,
            String oldValue, String newValue, Long performedBy) {
        log(entity, entityId, "UPDATE", field, oldValue, newValue, performedBy);
    }

    // -----------------------------
    // 📋 Lấy log (phân trang & all)
    // -----------------------------
    public Page<AuditLog> getAllLogs(int page, int size) {
//        return auditLogRepository.findAll(PageRequest.of(page, size));
        return auditLogRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"))
        );
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    // -----------------------------
    // 👤 Lấy user hiện tại đúng chuẩn
    // -----------------------------
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof User user) {
            return user;
        } else if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
            String username = springUser.getUsername();
            return userRepository.findByEmail(username)
                    .or(() -> userRepository.findByUsername(username))
                    .orElse(null);
        } else if (principal instanceof String username) {
            return userRepository.findByEmail(username)
                    .or(() -> userRepository.findByUsername(username))
                    .orElse(null);
        }

        return null;
    }

    // -----------------------------
    // 📎 Lấy ID user hiện tại (nếu có)
    // -----------------------------
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId().longValue() : 0L;
    }
}
