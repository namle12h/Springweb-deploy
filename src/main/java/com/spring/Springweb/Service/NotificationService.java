/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.NotificationDTO;
import com.spring.Springweb.Entity.Notification;
import java.util.List;

public interface NotificationService {

    Notification createNotification(NotificationDTO dto);

    List<Notification> getNotificationsByUser(Long targetId);

    Notification markAsRead(Long id);

    void markAllAsRead(Long targetId);

    void deleteAll(Long targetId);
}
