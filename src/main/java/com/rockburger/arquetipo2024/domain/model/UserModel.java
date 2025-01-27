package com.rockburger.arquetipo2024.domain.model;

import java.time.LocalDate;
import com.rockburger.arquetipo2024.domain.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.Period;

public class UserModel {
    private static final Logger logger = LoggerFactory.getLogger(UserModel.class);
    private Long id;
    private String firstName;
    private String lastName;
    private String idDocument;
    private String phoneNumber;
    private LocalDate birthDate;
    private String email;
    private String password;
    private String role;

    public UserModel(String firstName, String lastName, String idDocument, String phoneNumber, LocalDate birthDate, String email, String password, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.idDocument = idDocument;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public UserModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getIdDocument() {
        return idDocument;
    }

    public void setIdDocument(String idDocument) {
        this.idDocument = idDocument;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void validateAge() {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18) {
            logger.error("Invalid age: User must be at least 18 years old");
            throw new InvalidAgeException("User must be at least 18 years old");
        }
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }


}
