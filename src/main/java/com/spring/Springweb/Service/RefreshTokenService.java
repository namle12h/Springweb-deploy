/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.spring.Springweb.Entity.RefreshToken;

@Service
public interface RefreshTokenService {

    public RefreshToken createRefreshToken(String email);

    public Optional<RefreshToken> findByToken(String token);

    public boolean isValid(RefreshToken token);

    public void deleteByEmail(String email);

    public RefreshToken save(RefreshToken refreshToken);

    public RefreshToken updateRefreshToken(RefreshToken refreshToken);
}
