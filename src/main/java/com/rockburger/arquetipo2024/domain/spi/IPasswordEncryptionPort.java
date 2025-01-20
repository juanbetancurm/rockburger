package com.rockburger.arquetipo2024.domain.spi;

public interface IPasswordEncryptionPort {
    String encryptPassword(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}