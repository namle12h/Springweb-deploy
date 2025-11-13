/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.spring.Springweb.Entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByTargetIdOrderByCreatedAtDesc(Long targetId);

    @Query("""
SELECT n FROM Notification n
WHERE 
    n.isDeleted = false
    AND (
        n.targetId = :targetId
        OR (n.type = 'SYSTEM' AND (
            :role IN ('ADMIN', 'STAFF')
        ))
    )
ORDER BY n.createdAt DESC
""")
    List<Notification> findAllForUserOrSystem(@Param("targetId") Long targetId, @Param("role") String role);

    @Modifying
    @Transactional
    @Query("""
    UPDATE Notification n 
    SET n.isDeleted = true 
    WHERE n.targetId = :userId
""")
    void softDeleteAllByUser(@Param("userId") Long userId);

}
