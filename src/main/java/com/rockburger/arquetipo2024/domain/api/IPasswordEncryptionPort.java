package com.rockburger.arquetipo2024.domain.api;

public interface IPasswordEncryptionPort {
    String encryptPassword(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
