package com.rockburger.burgermain.domain.spi;

public interface IPasswordEncryptionPort {
    String encryptPassword(String password);
    boolean matches(String rawPassword, String encodedPassword);
}