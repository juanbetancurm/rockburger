package com.rockburger.burgermain.domain.spi;

public interface IPasswordEncryptionPort {
    /**
     * Encrypts a raw password
     * @param rawPassword the raw password to encrypt
     * @return the encrypted password
     */
    String encryptPassword(String rawPassword);

    /**
     * Matches a raw password with an encrypted password
     * @param rawPassword the raw password
     * @param encryptedPassword the encrypted password
     * @return true if passwords match, false otherwise
     */
    boolean matches(String rawPassword, String encryptedPassword);
}