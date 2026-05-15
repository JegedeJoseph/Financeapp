package com.financeapp.services;

import com.financeapp.dto.response.ForecastResponse;
import com.financeapp.dto.response.SpendingPatternResponse;
import com.financeapp.ml.MLServiceClient;
import com.financeapp.ml.dto.ExpensePredictionResponse;
import com.financeapp.models.ForecastHistory;
import com.financeapp.models.Transaction;
import com.financeapp.models.User;
import com.financeapp.repositories.ForecastHistoryRepository;
import com.financeapp.repositories.TransactionRepository;
import com.financeapp.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final MLServiceClient mlServiceClient;
    private final TransactionRepository transactionRepository;
    private final ForecastHistoryRepository forecastHistoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ForecastResponse getExpenseForecast(Long userId, String period) {
        log.info("Getting expense forecast for user: {}, period: {}", userId, period);

        // Try ML service first
        try {
            ExpensePredictionResponse mlResponse = mlServiceClient.predictExpenses(userId, period);

            Map<String, BigDecimal> categoryPredictions = new HashMap<>();
            mlResponse.getPredictedExpenses().forEach((cat, val) ->
                    categoryPredictions.put(cat, BigDecimal.valueOf(val))
            );

            return ForecastResponse.builder()
                    .period(period)
                    .predictedTotal(BigDecimal.valueOf(mlResponse.getTotalPredicted()))
                    .categoryPredictions(categoryPredictions)
                    .confidence(mlResponse.getConfidence())
                    .recommendation(mlResponse.getRecommendation())
                    .build();

        } catch (Exception e) {
            log.error("ML forecast failed, using simple average", e);
            return getSimpleForecast(userId, period);
        }
    }

    private ForecastResponse getSimpleForecast(Long userId, String period) {
        // Calculate 3-month average
        YearMonth currentMonth = YearMonth.now();
        BigDecimal totalSpending = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryAverages = new HashMap<>();
        int monthsCount = 0;

        for (int i = 1; i <= 3; i++) {
            YearMonth month = currentMonth.minusMonths(i);
            List<Object[]> summary = transactionRepository.getCategorySpendingSummary(
                    userId,
                    month.atDay(1).atStartOfDay(),
                    month.atEndOfMonth().atTime(23, 59, 59)
            );

            if (!summary.isEmpty()) {
                monthsCount++;
                for (Object[] row : summary) {
                    String category = row[0].toString();
                    BigDecimal amount = (BigDecimal) row[1];
                    totalSpending = totalSpending.add(amount);
                    categoryAverages.merge(category, amount, BigDecimal::add);
                }
            }
        }

        if (monthsCount > 0) {
            totalSpending = totalSpending.divide(BigDecimal.valueOf(monthsCount), 2, BigDecimal.ROUND_HALF_UP);
            int finalMonthsCount = monthsCount;
            categoryAverages.forEach((cat, total) ->
                    categoryAverages.put(cat, total.divide(BigDecimal.valueOf(finalMonthsCount), 2, BigDecimal.ROUND_HALF_UP))
            );
        }

        return ForecastResponse.builder()
                .period(period)
                .predictedTotal(totalSpending)
                .categoryPredictions(categoryAverages)
                .confidence(0.3)
                .recommendation("Based on simple average of last " + monthsCount + " months")
                .build();
    }

    @Transactional
    public void saveForecast(Long userId, ForecastResponse forecast) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        try {
            ForecastHistory history = ForecastHistory.builder()
                    .user(user)
                    .forecastPeriod(forecast.getPeriod())
                    .predictedTotal(forecast.getPredictedTotal())
                    .categoryPredictions(objectMapper.writeValueAsString(forecast.getCategoryPredictions()))
                    .confidence(forecast.getConfidence())
                    .recommendations(forecast.getRecommendation())
                    .build();

            forecastHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("Failed to save forecast history", e);
        }
    }

    public List<SpendingPatternResponse> getSpendingPatterns(Long userId) {
        List<SpendingPatternResponse> patterns = new ArrayList<>();

        // Daily pattern
        YearMonth currentMonth = YearMonth.now();
        List<Object[]> dailySummary = transactionRepository.getCategorySpendingSummary(
                userId,
                currentMonth.atDay(1).atStartOfDay(),
                currentMonth.atEndOfMonth().atTime(23, 59, 59)
        );

        if (!dailySummary.isEmpty()) {
            Map<String, BigDecimal> categoryMap = new HashMap<>();
            dailySummary.forEach(row -> categoryMap.put(row[0].toString(), (BigDecimal) row[1]));

            // Find top categories
            List<Map.Entry<String, BigDecimal>> sorted = categoryMap.entrySet().stream()
                    .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                    .limit(3)
                    .collect(Collectors.toList());

            Map<String, Object> details = new HashMap<>();
            details.put("topCategories", sorted);
            details.put("totalCategories", categoryMap.size());

            List<String> recommendations = new ArrayList<>();
            if (!sorted.isEmpty()) {
                recommendations.add("Focus on reducing " + sorted.get(0).getKey() + " spending");
            }
            recommendations.add("Set budgets for your top spending categories");

            patterns.add(SpendingPatternResponse.builder()
                    .patternType("CATEGORY_ANALYSIS")
                    .description("Your top spending categories this month")
                    .details(details)
                    .recommendations(recommendations)
                    .build());
        }

        return patterns;
    }
}
