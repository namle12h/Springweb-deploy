package com.spring.Springweb.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.Springweb.Entity.RefreshToken;
import com.spring.Springweb.Repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author ADMIN
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // 🔹 Tạo refresh token mới
    @Override
    public RefreshToken createRefreshToken(String email) {
        String token = UUID.randomUUID().toString();  // Tạo refresh token ngẫu nhiên

        // Đặt thời gian hết hạn là 30 ngày từ thời điểm hiện tại
        Instant expiryDate = Instant.now().plus(2, ChronoUnit.MINUTES);  // 30 ngày từ thời điểm hiện tại

        // Tạo và lưu refresh token vào database
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setEmail(email);
        refreshToken.setExpiryDate(expiryDate);  // Set thời gian hết hạn cố định

        return refreshTokenRepository.save(refreshToken);  // Lưu vào DB
    }

    // 🔹 Tìm token trong DB
    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // 🔹 Kiểm tra token còn hạn không
    @Override
    public boolean isValid(RefreshToken token) {
        return token.getExpiryDate().isAfter(Instant.now());
    }

    // 🔹 Xóa token khi 
    @Override
    public void deleteByEmail(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }

    @Override
    @Transactional
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public RefreshToken updateRefreshToken(RefreshToken refreshToken) {
        Optional<RefreshToken> existingToken = refreshTokenRepository.findById(refreshToken.getId());
        if (existingToken.isPresent()) {
            RefreshToken tokenToUpdate = existingToken.get();
            tokenToUpdate.setRefreshCount(refreshToken.getRefreshCount());  // Chỉ cập nhật refreshCount
             tokenToUpdate.setExpiryDate(refreshToken.getExpiryDate());  // Nếu cần update thêm các giá trị khác
            return refreshTokenRepository.save(tokenToUpdate);  // Save lại token đã cập nhật
        }
        throw new RuntimeException("Refresh token not found");
    }
}
