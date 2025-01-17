package com.rockburger.arquetipo2024.domain.model;


import com.rockburger.arquetipo2024.domain.exception.BlankFieldException;
import com.rockburger.arquetipo2024.domain.exception.EmptyFieldException;
import com.rockburger.arquetipo2024.domain.util.DomainConstants;

public class BrandModel {
    private Long id;
    private String name;
    private String description;

    public BrandModel(Long id, String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new BlankFieldException(DomainConstants.Field.NAME.toString());
        }
        if (name.trim().length() > 50) {
            throw new IllegalArgumentException("Name cannot exceed 50 characters");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new EmptyFieldException(DomainConstants.Field.DESCRIPTION.toString());
        }
        if (description.trim().length() > 90) {
            throw new IllegalArgumentException("Description cannot exceed 90 characters");
        }
        this.id = id;
        this.name = name.trim();
        this.description = description.trim();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
