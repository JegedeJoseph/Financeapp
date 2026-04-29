package com.financeapp.services;



import com.financeapp.models.Budget;
import com.financeapp.models.Insight;
import com.financeapp.models.SavingsGoal;
import com.financeapp.models.User;
import com.financeapp.models.enums.InsightType;
import com.financeapp.models.enums.TransactionCategory;
import com.financeapp.repositories.BudgetRepository;
import com.financeapp.repositories.InsightRepository;
import com.financeapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsightService {

    private final InsightRepository insightRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @Transactional
    public void checkBudgetOverspending(Long userId, TransactionCategory category, YearMonth yearMonth) {
        List<Budget> overspentBudgets = budgetRepository.findOverspentBudgets(
                userId, yearMonth.getMonthValue(), yearMonth.getYear());

        for (Budget budget : overspentBudgets) {
            createOverspendingInsight(userId, budget);
            budget.setAlertSent(true);
            budgetRepository.save(budget);
        }
    }

    @Transactional
    public void createOverspendingInsight(Long userId, Budget budget) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Map<String, String> metadata = new HashMap<>();
        metadata.put("category", budget.getCategory().name());
        metadata.put("limit", budget.getMonthlyLimit().toString());
        metadata.put("spent", budget.getCurrentSpending().toString());
        metadata.put("month", budget.getMonth().toString());
        metadata.put("year", budget.getYear().toString());

        String message = String.format(
                "⚠️ Overspending Alert: You've exceeded your %s budget! " +
                        "Spent: $%.2f / Limit: $%.2f",
                budget.getCategory().name().toLowerCase(),
                budget.getCurrentSpending(),
                budget.getMonthlyLimit()
        );

        Insight insight = Insight.builder()
                .user(user)
                .message(message)
                .insightType(InsightType.OVERSPENDING_ALERT)
                .metadata(metadata)
                .read(false)
                .build();

        insightRepository.save(insight);
        log.info("Created overspending insight for user: {}", userId);
    }

    @Transactional
    public void createGoalMilestoneInsight(Long userId, SavingsGoal goal) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || goal.isNotificationSent()) return;

        String message = String.format(
                "🎯 You're almost there! You've reached 90%% of your savings goal: %s",
                goal.getGoalName()
        );

        Insight insight = Insight.builder()
                .user(user)
                .message(message)
                .insightType(InsightType.SAVINGS_MILESTONE)
                .build();

        insightRepository.save(insight);
        goal.setNotificationSent(true);
    }

    @Transactional
    public void createGoalAchievedInsight(Long userId, SavingsGoal goal) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        String message = String.format(
                "🎉 Congratulations! You've achieved your savings goal: %s!",
                goal.getGoalName()
        );

        Insight insight = Insight.builder()
                .user(user)
                .message(message)
                .insightType(InsightType.SAVINGS_MILESTONE)
                .build();

        insightRepository.save(insight);
    }
}
