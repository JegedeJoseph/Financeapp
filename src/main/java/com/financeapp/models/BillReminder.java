package com.financeapp.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bill_reminders", indexes = {
        @Index(name = "idx_bill_user", columnList = "user_id"),
        @Index(name = "idx_bill_due_date", columnList = "due_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String billName;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(length = 50)
    private String recurrence; // MONTHLY, WEEKLY, YEARLY, ONCE

    @Column(length = 200)
    private String category;

    @Builder.Default
    private boolean paid = false;

    @Builder.Default
    private boolean notificationSent = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
