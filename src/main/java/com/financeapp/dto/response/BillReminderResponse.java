package com.financeapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillReminderResponse {
    private Long id;
    private String billName;
    private BigDecimal amount;
    private LocalDate dueDate;
    private long daysUntilDue;
    private String recurrence;
    private String category;
    private boolean paid;
    private boolean isOverdue;
}