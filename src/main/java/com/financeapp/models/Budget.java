package com.financeapp.models;


import com.financeapp.models.enums.TransactionCategory;
import com.financeapp.utils.TransactionCategoryConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets", indexes = {
        @Index(name = "idx_budget_user", columnList = "user_id"),
        @Index(name = "idx_budget_category", columnList = "category")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "category", "month", "year"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Convert(converter = TransactionCategoryConverter.class)
    @Column(nullable = false)
    private TransactionCategory category;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyLimit;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal currentSpending = BigDecimal.ZERO;

    @Column(name = "budget_month", nullable = false)
    private Integer month;

    @Column(name = "budget_year", nullable = false)
    private Integer year;

    private boolean alertSent = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder.Default
    private boolean active = true;
}