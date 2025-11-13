/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.ReviewDTO;
import com.spring.Springweb.Entity.Review;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author ADMIN
 */
public interface ReviewService {

    public List<Review> getAllReviews();
            
    public List<Review> getReviews(String reviewType, Integer refId, boolean onlyApproved);

    public Review addReview(Review review);

    public Review updateReview(Integer id, Review updated);

    public boolean deleteReview(Integer id);

    public Optional<Review> approveReview(Integer id);

    public Review addReviewWithImage(ReviewDTO dto, MultipartFile file) throws IOException;
}
