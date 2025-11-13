package com.spring.Springweb.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "mysecret123";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10h
//        private static final long EXPIRATION_TIME = 1000 * 60 * 1; // 10h


    // generate token chỉ có username
    public String generateToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    // generate token có thêm claims (ví dụ role)
    public String generateToken(Map<String, Object> extraClaims, String subject) {
        com.auth0.jwt.JWTCreator.Builder builder = JWT.create()
                .withSubject(subject)
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME));

        // duyệt từng claim
        extraClaims.forEach((key, value) -> {
            if (value instanceof String s) {
                builder.withClaim(key, s);
            } else if (value instanceof Integer i) {
                builder.withClaim(key, i);
            } else if (value instanceof Long l) {
                builder.withClaim(key, l);
            } else if (value instanceof Boolean b) {
                builder.withClaim(key, b);
            } else if (value instanceof Double d) {
                builder.withClaim(key, d);
            } else if (value instanceof Date dt) {
                builder.withClaim(key, dt);
            } else if (value instanceof Map<?, ?> m) {
                // cần ép kiểu cẩn thận, giả sử value là Map<String, ?>
                builder.withClaim(key, (Map<String, ?>) m);
            } else if (value instanceof java.util.List<?> list) {
                builder.withClaim(key, list);
            } else {
                throw new IllegalArgumentException("Unsupported claim type for key: " + key);
            }
        });

        return builder.sign(Algorithm.HMAC256(SECRET_KEY));
    }

    public String extractUsername(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token);
        return decodedJWT.getSubject();
    }

    public String extractRole(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token);
        return decodedJWT.getClaim("role").asString();
    }

    public boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token);
        return decodedJWT.getExpiresAt().before(new Date());
    }

    public boolean validateToken(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }
}
