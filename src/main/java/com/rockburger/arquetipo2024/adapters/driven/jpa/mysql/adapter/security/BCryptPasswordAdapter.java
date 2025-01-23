package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.adapter.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import com.rockburger.arquetipo2024.domain.spi.IPasswordEncryptionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class BCryptPasswordAdapter implements IPasswordEncryptionPort {
    private static final Logger logger = LoggerFactory.getLogger(BCryptPasswordAdapter.class);
    private final BCryptPasswordEncoder passwordEncoder;

    public BCryptPasswordAdapter(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encryptPassword(String rawPassword) {
        logger.debug("Encrypting password");
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        logger.debug("Verifying password match");
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

