package com.financeapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    private Long id;
    private String category;
    private BigDecimal monthlyLimit;
    private BigDecimal currentSpending;
    private BigDecimal remaining;
    private Double percentageUsed;
    private Integer month;
    private Integer year;
    private boolean overspent;

    @Builder.Default
    private boolean active = true;
}
