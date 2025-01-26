package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.adapter.security;


import com.rockburger.arquetipo2024.configuration.security.JwtKeyProvider;
import com.rockburger.arquetipo2024.domain.exception.JwtGenerationException;
import com.rockburger.arquetipo2024.domain.model.UserModel;
import com.rockburger.arquetipo2024.domain.spi.IJwtPersistencePort;
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

    public JwtAdapter(JwtKeyProvider jwtKeyProvider) {
        this.jwtKeyProvider = jwtKeyProvider;
    }

    @Override
    public String generateToken(UserModel userModel, String secret, int expiration) {
        try {
            SecretKey key = jwtKeyProvider.getSigningKey(secret);

            return Jwts.builder()
                    .setSubject(userModel.getEmail())
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
        return getClaimsFromToken(token, secret).getSubject();
    }

    @Override
    public String getRoleFromToken(String token, String secret) {
        Claims claims = getClaimsFromToken(token, secret);
        return claims.get("role", String.class);
    }

    @Override
    public boolean isTokenValid(String token, String secret) {
        try {
            SecretKey key = jwtKeyProvider.getSigningKey(secret);
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
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
