package com.rockburger.arquetipo2024.configuration.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JwtKeyProvider {

    public SecretKey getSigningKey(String secret) {
        byte[] keyBytes = Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String generateKey() {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}