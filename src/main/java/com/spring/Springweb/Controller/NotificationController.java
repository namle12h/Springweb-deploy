/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Controller;
import com.spring.Springweb.DTO.NotificationDTO;
import com.spring.Springweb.Entity.Notification;
import com.spring.Springweb.Repository.ApiResponse;
import com.spring.Springweb.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ApiResponse<Notification> create(@RequestBody NotificationDTO dto) {
        return ApiResponse.ok("Tạo thông báo thành công", notificationService.createNotification(dto));
    }

    @GetMapping("/{userId}")
    public ApiResponse<List<Notification>> getByUser(@PathVariable Long userId) {
        return ApiResponse.ok("Danh sách thông báo", notificationService.getNotificationsByUser(userId));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Notification> markAsRead(@PathVariable Long id) {
        return ApiResponse.ok("Đã đánh dấu đã đọc", notificationService.markAsRead(id));
    }

    @PutMapping("/user/{userId}/read-all")
    public ApiResponse<String> markAllRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.ok("Đã đánh dấu tất cả là đã đọc", null);
    }

    @PutMapping("/user/{userId}")
    public ApiResponse<String> deleteAll(@PathVariable Long userId) {
        notificationService.deleteAll(userId);
        return ApiResponse.ok("Đã xóa tất cả thông báo", null);
    }
}
