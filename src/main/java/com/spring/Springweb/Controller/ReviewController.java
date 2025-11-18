package com.spring.Springweb.Controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.spring.Springweb.DTO.ReviewDTO;
import com.spring.Springweb.Entity.Review;
import com.spring.Springweb.Service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;


    @GetMapping
    public ResponseEntity<List<Review>> getReviews(
            @RequestParam String type, // "Product" hoặc "Service"
            @RequestParam Integer refId,
            @RequestParam(defaultValue = "true") boolean approvedOnly
    ) {
        return ResponseEntity.ok(reviewService.getReviews(type, refId, approvedOnly));
    }


    // 🔹 Sửa review
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(
            @PathVariable Integer id,
            @RequestBody Review updated
    ) {
        return ResponseEntity.ok(reviewService.updateReview(id, updated));
    }

    // 🔹 Duyệt review (Admin)
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Review> approveReview(@PathVariable Integer id) {
        return reviewService.approveReview(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔹 Xóa review
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer id) {
        return reviewService.deleteReview(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // @PostMapping(consumes = {"multipart/form-data"})
     @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createReview(
            @RequestPart("data") ReviewDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        Review saved = reviewService.addReviewWithImage(dto, file);
        return ResponseEntity.ok(saved);
    }
}
