package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.NotificationDTO;
import com.spring.Springweb.DTO.ReviewDTO;
import com.spring.Springweb.Entity.Review;
import com.spring.Springweb.Entity.User;
import com.spring.Springweb.Repository.ReviewRepository;
import com.spring.Springweb.Repository.UserRepository;
import com.spring.Springweb.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;
    private final NotificationService notificationService;

    @Override
    public List<Review> getAllReviews() {
        return reviewRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public List<Review> getReviews(String reviewType, Integer refId, boolean onlyApproved) {
        if (onlyApproved) {
            return reviewRepository.findByReviewTypeAndRefIdAndIsApprovedTrueOrderByCreatedAtDesc(reviewType, refId);
        }
        return reviewRepository.findByReviewTypeAndRefId(reviewType, refId);
    }

    @Override
    public Review addReview(Review review) {
        // Tìm customer theo ID từ JSON

        User customer = userRepository.findById(review.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        review.setCustomer(customer);
        review.setCreatedAt(new java.util.Date());
        review.setIsApproved(true); // cần admin duyệt

        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review addReviewWithImage(ReviewDTO dto, MultipartFile file) throws IOException {
        // 🧩 1. Lấy user hiện tại từ token (hoặc fallback theo id nếu có)
        User customer = getCurrentUser();
        if (customer == null && dto.getCustomerId() != null) {
            customer = userRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
        }

        // 🧩 2. Upload ảnh/video nếu có
        String uploadedUrl = null;
        if (file != null && !file.isEmpty()) {
            uploadedUrl = imageService.uploadImage(file);
            System.out.println("📸 Uploaded file: " + uploadedUrl);
        }

        // 🧩 3. Tạo review entity
        Review review = new Review();
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCustomer(customer);
        review.setRefId(dto.getRefId());
        review.setReviewType(dto.getReviewType());
        review.setImageUrl(uploadedUrl);
        review.setCreatedAt(new java.util.Date());
        review.setIsApproved(true); // ✅ tự duyệt tạm, hoặc false nếu cần admin
        review.setReply(null);

        // 🧩 4. Lưu vào DB
        // 🧩 4. Lưu vào DB
        Review saved = reviewRepository.save(review);

        // 🧩 5. Gửi thông báo sau khi lưu thành công
        try {
            NotificationDTO noti = NotificationDTO.builder()
                    .title("Đánh giá mới 🌟")
                    .message("Khách hàng " + (customer != null ? customer.getName() : "Ẩn danh")
                            + " vừa gửi một đánh giá mới cho dịch vụ!")
                    .type("SYSTEM")
                    .entityType("Review")
                    .entityId(saved.getId().longValue())
                    .targetId(null) // 👉 Gửi cho admin hoặc bạn có thể chọn targetId động
                    .build();

            notificationService.createNotification(noti);
            System.out.println("✅ Notification created for review id: " + saved.getId());
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo thông báo review: " + e.getMessage());
        }

        return saved;
    }

    @Override
    public Review updateReview(Integer id, Review updated) {
        return reviewRepository.findById(id).map(r -> {
            r.setComment(updated.getComment());
            r.setRating(updated.getRating());
            r.setImageUrl(updated.getImageUrl());
            r.setUpdatedAt(new java.util.Date());
            return reviewRepository.save(r);
        }).orElseThrow(() -> new RuntimeException("Review not found"));
    }

    @Override
    public boolean deleteReview(Integer id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<Review> approveReview(Integer id) {
        return reviewRepository.findById(id).map(r -> {
            r.setIsApproved(true);
            r.setUpdatedAt(new java.util.Date());
            return reviewRepository.save(r);
        });
    }

    private User getCurrentUser() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7).trim();
        try {
            String username = jwtUtil.extractUsername(token);
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        } catch (Exception e) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }
    }

}
