/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.NotificationDTO;
import com.spring.Springweb.Entity.Notification;
import com.spring.Springweb.Entity.User;
import com.spring.Springweb.Repository.NotificationRepository;
import com.spring.Springweb.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public Notification createNotification(NotificationDTO dto) {
        Notification n = Notification.builder()
                .title(dto.getTitle())
                .message(dto.getMessage())
                .type(dto.getType())
                .targetId(dto.getTargetId())
                .entityType(dto.getEntityType())
                .entityId(dto.getEntityId())
                .isRead(false)
                .build();

        return notificationRepository.save(n);
    }

    @Override
    public List<Notification> getNotificationsByUser(Long targetId) {
        // return notificationRepository.findByTargetIdOrderByCreatedAtDesc(targetId);
        User user = userRepository.findById(targetId.intValue())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.findAllForUserOrSystem(targetId, user.getRole());
    }

    @Override
    public Notification markAsRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        return notificationRepository.save(n);
    }

    @Override
    public void markAllAsRead(Long targetId) {
        List<Notification> list = notificationRepository.findByTargetIdOrderByCreatedAtDesc(targetId);
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
    }

    @Override
    @Transactional
    public void deleteAll(Long targetId) {
         notificationRepository.softDeleteAllByUser(targetId);
    }
}
