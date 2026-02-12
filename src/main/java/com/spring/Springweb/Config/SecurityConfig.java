/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.spring.Springweb.filter.JwtFilter;
import java.util.Arrays;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Value("${cors.allowed-origins}")
    private String corsAllowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                }) // dùng bean corsConfigurationSource bên dưới

                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                .requestMatchers("/", "/home", "/index.html", "/css/**", "/js/**").permitAll()
                .requestMatchers("/api/auth/get-profile", "/api/auth/update-profile").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/customers").permitAll()
                .requestMatchers("/api/stats/**").permitAll()
                .requestMatchers("/health/**").permitAll()
                .requestMatchers("/api/reviews/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/customers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/customers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/staff/create").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/services/**").permitAll()
                .requestMatchers("/api/rooms/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/staff/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/services/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/orders/**").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.GET, "/api/orders").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.GET, "/api/orders/all").hasAnyRole("STAFF", "ADMIN")
                //                .requestMatchers( "/api/invoices/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/invoices/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/invoices/**").permitAll()
                .requestMatchers("/api/appointments/**").permitAll()
                .requestMatchers("/api/history/**").permitAll()
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers("/api/notifications/**").permitAll()
                .requestMatchers("/create-payment/**").permitAll()
                .requestMatchers("/payment-return/**").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // add jwt filter trước filter xác thực mặc định
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim) // 🔥 QUAN TRỌNG
                .toList();

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
