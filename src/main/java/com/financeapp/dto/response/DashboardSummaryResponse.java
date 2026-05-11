package com.financeapp.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private BigDecimal totalBalance;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal netSavings;
    private Map<String, BigDecimal> spendingByCategory;
    private int activeBudgets;
    private int budgetsAtRisk; // budgets > 80% used
    private int activeSavingsGoals;
    private BigDecimal totalSaved;
    private int upcomingBills;
    private String topInsight;
}
