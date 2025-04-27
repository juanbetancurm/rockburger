package com.rockburger.burgermain.domain.api;

public interface IPasswordEncryptionPort {
    String encryptPassword(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
