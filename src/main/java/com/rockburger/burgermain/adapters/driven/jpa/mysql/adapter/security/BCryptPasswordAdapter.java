package com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.rockburger.burgermain.domain.spi.IPasswordEncryptionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class BCryptPasswordAdapter implements IPasswordEncryptionPort {
    private static final Logger logger = LoggerFactory.getLogger(BCryptPasswordAdapter.class);
    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encryptPassword(String rawPassword) {
        logger.debug("Encrypting password");
        if (rawPassword == null) {
            throw new IllegalArgumentException("Raw password cannot be null");
        }
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encryptedPassword) {
        logger.debug("Verifying password match");
        if (rawPassword == null || encryptedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encryptedPassword);
    }
}