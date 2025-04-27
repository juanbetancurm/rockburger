package com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter.security;


import com.rockburger.burgermain.configuration.security.JwtKeyProvider;
import com.rockburger.burgermain.domain.exception.JwtGenerationException;
import com.rockburger.burgermain.domain.model.UserModel;
import com.rockburger.burgermain.domain.spi.IJwtPersistencePort;
import io.jsonwebtoken.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtAdapter implements IJwtPersistencePort {
    private static final Logger logger = LoggerFactory.getLogger(JwtAdapter.class);
    private final JwtKeyProvider jwtKeyProvider;
    private final String jwtSecret;

    public JwtAdapter(JwtKeyProvider jwtKeyProvider, String jwtSecretKey) {
        this.jwtKeyProvider = jwtKeyProvider;
        this.jwtSecret = jwtSecretKey;
    }

    @Override
    public String generateToken(UserModel userModel, String secret, int expiration) {
        try {
            // Use injected secret if provided one is null
            if (secret == null || secret.isEmpty()) {
                secret = this.jwtSecret;
            }

            SecretKey key = jwtKeyProvider.getSigningKey(secret);

            return Jwts.builder()
                    .setSubject(userModel.getEmail())
                    .claim("userId", userModel.getId())
                    .claim("role", userModel.getRole())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating JWT token", e);
            throw new JwtGenerationException("Error generating JWT token", e);
        }
    }

    @Override
    public String getUsernameFromToken(String token, String secret) {
        if (secret == null || secret.isEmpty()) {
            secret = this.jwtSecret;
        }
        return getClaimsFromToken(token, secret).getSubject();
    }

    @Override
    public String getRoleFromToken(String token, String secret) {
        if (secret == null || secret.isEmpty()) {
            secret = this.jwtSecret;
        }
        Claims claims = getClaimsFromToken(token, secret);
        return claims.get("role", String.class);
    }

    @Override
    public boolean isTokenValid(String token, String secret) {
        try {
            if (secret == null || secret.isEmpty()) {
                secret = this.jwtSecret;
            }

            SecretKey key = jwtKeyProvider.getSigningKey(secret);
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaimsFromToken(String token, String secret) {
        SecretKey key = jwtKeyProvider.getSigningKey(secret);
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}