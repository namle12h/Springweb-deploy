package com.spring.Springweb.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spring.Springweb.Entity.Review;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // Lấy tất cả review theo loại (Product/Service) và refId
    List<Review> findByReviewTypeAndRefId(String reviewType, Integer refId);

    // Lấy các review đã được duyệt
    List<Review> findByReviewTypeAndRefIdAndIsApprovedTrueOrderByCreatedAtDesc(String reviewType, Integer refId);
    
    
    @Query("""
        SELECT AVG(r.rating), 0
        FROM Review r
        WHERE r.createdAt BETWEEN :start AND :end
    """)
    Double getAverageRating(LocalDateTime start, LocalDateTime end);

    // Đánh giá trung bình dịch vụ
    @Query("""
        SELECT AVG(r.rating), 0
        FROM Review r
        WHERE r.reviewType = 'Service'
          AND r.createdAt BETWEEN :start AND :end
    """)
    Double getAverageServiceRating(LocalDateTime start, LocalDateTime end);

    // Đánh giá trung bình sản phẩm
    @Query("""
        SELECT AVG(r.rating), 0
        FROM Review r
        WHERE r.reviewType = 'Product'
          AND r.createdAt BETWEEN :start AND :end 
    """)
    Double getAverageProductRating(LocalDateTime start, LocalDateTime end);
}
