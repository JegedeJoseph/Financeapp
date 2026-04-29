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
public class SpendingAnalyticsResponse {

    private BigDecimal totalSpending;
    private BigDecimal averageDailySpending;
    private Map<String, BigDecimal> spendingByCategory;
    private Map<String, BigDecimal> budgetVsActual;
    private String highestSpendingCategory;
    private String spendingTrend;

    @Builder.Default
    private boolean active = true;
}
