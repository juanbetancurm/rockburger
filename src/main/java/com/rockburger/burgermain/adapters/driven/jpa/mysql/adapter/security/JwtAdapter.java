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

    // Token type constants
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    public JwtAdapter(JwtKeyProvider jwtKeyProvider, String jwtSecretKey) {
        this.jwtKeyProvider = jwtKeyProvider;
        this.jwtSecret = jwtSecretKey;
    }

    @Override
    public String generateToken(UserModel userModel, String secret, int expiration) {
        try {
            if (secret == null || secret.isEmpty()) {
                secret = this.jwtSecret;
            }

            SecretKey key = jwtKeyProvider.getSigningKey(secret);
            Date currentDate = new Date();
            Date expirationDate = new Date(System.currentTimeMillis() + expiration);

            String token = Jwts.builder()
                    .setSubject(userModel.getEmail())
                    .claim("userId", userModel.getId())
                    .claim("role", userModel.getRole())
                    .claim("tokenType", TOKEN_TYPE_ACCESS)
                    .setIssuedAt(currentDate)
                    .setExpiration(expirationDate)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();

            logger.debug("Generated access token for user: {} with expiration: {}",
                    userModel.getEmail(), expirationDate);
            return token;
        } catch (Exception e) {
            logger.error("Error generating JWT token for user: {}", userModel.getEmail(), e);
            throw new JwtGenerationException("Error generating JWT token", e);
        }
    }

    @Override
    public String generateRefreshToken(UserModel userModel, String secret, int expiration) {
        try {
            if (secret == null || secret.isEmpty()) {
                secret = this.jwtSecret;
            }

            SecretKey key = jwtKeyProvider.getSigningKey(secret);
            Date currentDate = new Date();
            Date expirationDate = new Date(System.currentTimeMillis() + expiration);

            String refreshToken = Jwts.builder()
                    .setSubject(userModel.getEmail())
                    .claim("userId", userModel.getId())
                    .claim("role", userModel.getRole())
                    .claim("tokenType", TOKEN_TYPE_REFRESH)
                    .setIssuedAt(currentDate)
                    .setExpiration(expirationDate)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();

            logger.debug("Generated refresh token for user: {} with expiration: {}",
                    userModel.getEmail(), expirationDate);
            return refreshToken;
        } catch (Exception e) {
            logger.error("Error generating JWT refresh token for user: {}", userModel.getEmail(), e);
            throw new JwtGenerationException("Error generating JWT refresh token", e);
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
    public Long getUserIdFromToken(String token, String secret) {
        if (secret == null || secret.isEmpty()) {
            secret = this.jwtSecret;
        }
        Claims claims = getClaimsFromToken(token, secret);
        Object userIdClaim = claims.get("userId");

        if (userIdClaim instanceof Integer) {
            return ((Integer) userIdClaim).longValue();
        } else if (userIdClaim instanceof Long) {
            return (Long) userIdClaim;
        } else if (userIdClaim instanceof String) {
            try {
                return Long.parseLong((String) userIdClaim);
            } catch (NumberFormatException e) {
                logger.error("Invalid userId format in token: {}", userIdClaim);
                return null;
            }
        }
        return null;
    }

    @Override
    public String getTokenType(String token, String secret) {
        if (secret == null || secret.isEmpty()) {
            secret = this.jwtSecret;
        }
        Claims claims = getClaimsFromToken(token, secret);
        String tokenType = claims.get("tokenType", String.class);
        return tokenType != null ? tokenType : TOKEN_TYPE_ACCESS; // Default to access if not specified
    }

    @Override
    public String getClaimFromToken(String token, String secret, String claimName) {
        if (secret == null || secret.isEmpty()) {
            secret = this.jwtSecret;
        }
        Claims claims = getClaimsFromToken(token, secret);
        return claims.get(claimName, String.class);
    }

    @Override
    public long getTokenExpirationTime(String token, String secret) {
        if (secret == null || secret.isEmpty()) {
            secret = this.jwtSecret;
        }
        Claims claims = getClaimsFromToken(token, secret);
        Date expiration = claims.getExpiration();
        return expiration != null ? expiration.getTime() : 0;
    }

    @Override
    public boolean isTokenExpired(String token, String secret) {
        try {
            if (secret == null || secret.isEmpty()) {
                secret = this.jwtSecret;
            }
            Date expiration = getClaimsFromToken(token, secret).getExpiration();
            boolean expired = expiration.before(new Date());

            if (expired) {
                logger.warn("JWT token expired at: {}. Current time: {}",
                        expiration, new Date());
            }

            return expired;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
            return true;
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true; // Consider invalid tokens as expired
        }
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

            // Additional check for expiration
            boolean expired = isTokenExpired(token, secret);
            boolean valid = !expired;

            if (!valid) {
                logger.debug("Token validation failed - token is expired");
            }

            return valid;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: JWT expired at {}. Current time: {}, a difference of {} milliseconds. Allowed clock skew: 0 milliseconds.",
                    e.getClaims().getExpiration(),
                    new Date(),
                    System.currentTimeMillis() - e.getClaims().getExpiration().getTime());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.error("JWT token is malformed: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("JWT token compact of handler are invalid: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during JWT validation: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaimsFromToken(String token, String secret) {
        try {
            if (secret == null || secret.isEmpty()) {
                secret = this.jwtSecret;
            }

            SecretKey key = jwtKeyProvider.getSigningKey(secret);
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.debug("Token expired, but returning claims for extraction: {}", e.getMessage());
            return e.getClaims(); // Still return claims even if expired, for extraction purposes
        } catch (Exception e) {
            logger.error("Error extracting claims from token: {}", e.getMessage());
            throw new JwtGenerationException("Error extracting claims from token", e);
        }
    }
}