package com.rockburger.burgermain.domain.spi;

public interface IPasswordPEncryptionPort {
    String encryptPassword(String password);
    boolean matches(String rawPassword, String encodedPassword);
}