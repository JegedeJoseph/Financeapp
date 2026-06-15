package com.financeapp.models;

import com.financeapp.models.enums.UserRole;
import com.financeapp.utils.UserRoleConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

    @Entity
    @Table(name = "users", indexes = {
            @Index(name = "idx_user_email", columnList = "email", unique = true)
    })
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 100)
        private String fullName;

        @Column(nullable = false, unique = true, length = 100)
        private String email;

        @Column(nullable = false)
        private String passwordHash;

        @Convert(converter = UserRoleConverter.class)   // if you created that converter
        @Column(nullable = false)
        private UserRole role;

        @CreationTimestamp
        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @UpdateTimestamp
        private LocalDateTime updatedAt;

        @Column(nullable = false)
        private boolean enabled = true;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<BankAccount> bankAccounts = new ArrayList<>();

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Budget> budgets = new ArrayList<>();

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<SavingsGoal> savingsGoals = new ArrayList<>();

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Insight> insights = new ArrayList<>();

        @Builder.Default
        private boolean active = true;
    }

