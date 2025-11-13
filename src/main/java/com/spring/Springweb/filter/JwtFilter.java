// /*
//  * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
//  * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
//  */
package com.spring.Springweb.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.spring.Springweb.Repository.UserRepository;
import com.spring.Springweb.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // Danh sách URL bỏ qua JWT
    private static final String[] PUBLIC_URLS = {
        "/api/auth/**",
        "/api/public/**",
        //        "/api/customers/**",
        "/api/appointments/**",
        "/api/rooms/**", // khách tự đăng ký
        "/api/products/**",
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // Nếu request thuộc PUBLIC_URLS thì cho qua luôn
        AntPathMatcher pathMatcher = new AntPathMatcher();
        for (String url : PUBLIC_URLS) {
            if (pathMatcher.match(url, path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token); // lấy role từ claim

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var user = userRepository.findByUsername(username).orElse(null);

                    if (user != null && jwtUtil.validateToken(token, user.getUsername())) {

                        String roles = user.getRole(); // ADMIN / STAFF / CUSTOMER

                        List<GrantedAuthority> authorities
                                = List.of(new SimpleGrantedAuthority("ROLE_" + roles));

                        UsernamePasswordAuthenticationToken authentication
                                = new UsernamePasswordAuthenticationToken(
                                        username, // principal
                                        null, // credentials
                                        authorities
                                );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                System.out.println("JWT invalid: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
        String pathtest = request.getServletPath();
        System.out.println(">>> ServletPath = " + pathtest);
        System.out.println(">>> RequestURI  = " + request.getRequestURI());

    }
}
