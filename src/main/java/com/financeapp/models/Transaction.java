package com.financeapp.models;

import com.financeapp.models.enums.TransactionCategory;
import com.financeapp.models.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

    @Entity
    @Table(name = "transactions", indexes = {
            @Index(name = "idx_transaction_account", columnList = "account_id"),
            @Index(name = "idx_transaction_date", columnList = "transaction_date"),
            @Index(name = "idx_transaction_category", columnList = "category"),
            @Index(name = "idx_transaction_type", columnList = "transaction_type")
    })
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class Transaction {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "account_id", nullable = false)
        private BankAccount account;

        @Column(nullable = false, precision = 19, scale = 4)
        private BigDecimal amount;

        @Column(length = 500)
        private String description;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private TransactionCategory category;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private TransactionType transactionType;

        @Column(nullable = false)
        private LocalDateTime transactionDate;

        @Column(length = 255)
        private String merchantName;

        @Column(columnDefinition = "TEXT")
        private String rawDescription;

        private Double categoryConfidence;

        @CreationTimestamp
        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @Builder.Default
        private boolean active = true;
    }

