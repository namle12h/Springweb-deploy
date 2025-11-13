/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.UpdateProfileDTO;
import com.spring.Springweb.Entity.User;
import com.spring.Springweb.Repository.UserRepository;
import com.spring.Springweb.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        if (user.getPasswordHash() != null) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        return userRepository.save(user);
    }

    public User getUser(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public User updateProfile(HttpServletRequest request, UpdateProfileDTO dto) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Thiếu token");
        }
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getDob() != null) {
            user.setDob(dto.getDob());
        }

        return userRepository.save(user);
    }

}
