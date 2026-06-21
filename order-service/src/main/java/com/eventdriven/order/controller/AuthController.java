package com.eventdriven.order.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    private long tokenValidityMillis;

    @PostConstruct
    public void init() {
        tokenValidityMillis = 1000 * 60 * 60; // 1 hour
    }

    @PostMapping("/token")
    public ResponseEntity<AuthTokenResponse> generateToken(@RequestBody AuthRequest request) {
        String token = Jwts.builder()
                .setSubject(request.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tokenValidityMillis))
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                .compact();

        return ResponseEntity.ok(new AuthTokenResponse(token));
    }

    @Data
    public static class AuthRequest {
        private String username;
    }

    @Data
    public static class AuthTokenResponse {
        private final String token;
    }
}
