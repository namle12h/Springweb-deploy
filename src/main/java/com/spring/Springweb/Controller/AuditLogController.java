package com.spring.Springweb.Controller;

import com.spring.Springweb.Entity.AuditLog;
import com.spring.Springweb.Service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;
    

    // 🟢 API: Lấy danh sách log (phân trang)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<AuditLog> logs = auditLogService.getAllLogs(page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("content", logs.getContent());
        response.put("totalElements", logs.getTotalElements());
        response.put("totalPages", logs.getTotalPages());
        response.put("page", logs.getNumber());
        return ResponseEntity.ok(response);
    }

    // 🟣 API: Lấy thống kê tổng quan (cho phần trên dashboard)
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<AuditLog> allLogs = auditLogService.getAllLogs();

        long total = allLogs.size();
        long success = allLogs.stream().filter(l -> "success".equalsIgnoreCase(l.getStatus())).count();
        long failed = allLogs.stream().filter(l -> "failed".equalsIgnoreCase(l.getStatus())).count();
        long activeUsers = allLogs.stream().map(AuditLog::getPerformedByName).distinct().count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("success", success);
        stats.put("failed", failed);
        stats.put("activeUsers", activeUsers);

        return ResponseEntity.ok(stats);
    }
}
