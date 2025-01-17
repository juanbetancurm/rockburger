package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "categories", uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
public class CategoryEntity {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 50, unique = true)
        @NotEmpty(message = "Name is required")
        @Size(max = 50, message = "Name must be less than or equal to 50 characters")
        private String name;

        @Column(nullable = false, length = 90)
        @NotEmpty(message = "Description is required")
        @Size(max = 90, message = "Description must be less than or equal to 90 characters")
        private String description;

        @ManyToMany(fetch = FetchType.EAGER)
        private Set<ArticleEntity> articles = new HashSet<>();

}
