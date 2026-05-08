package com.financeapp.services;

import com.financeapp.dto.response.DashboardSummaryResponse;
import com.financeapp.models.*;
import com.financeapp.models.enums.TransactionType;
import com.financeapp.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final BillReminderRepository billReminderRepository;
    private final InsightRepository insightRepository;

    public DashboardSummaryResponse getDashboardSummary(Long userId) {
        YearMonth currentMonth = YearMonth.now();

        // Total balance across all accounts
        BigDecimal totalBalance = bankAccountRepository.calculateTotalBalanceByUser(userId);

        // Monthly income
        BigDecimal monthlyIncome = transactionRepository.calculateTotalSpending(
                userId,
                currentMonth.atDay(1).atStartOfDay(),
                currentMonth.atEndOfMonth().atTime(23, 59, 59)
        );

        // Get only income (positive amounts)
        // For simplicity, we'll calculate expenses and income separately
        BigDecimal monthlyExpenses = transactionRepository.calculateTotalSpending(
                userId,
                currentMonth.atDay(1).atStartOfDay(),
                currentMonth.atEndOfMonth().atTime(23, 59, 59)
        );

        // Category spending
        List<Object[]> categorySpending = transactionRepository.getCategorySpendingSummary(
                userId,
                currentMonth.atDay(1).atStartOfDay(),
                currentMonth.atEndOfMonth().atTime(23, 59, 59)
        );

        Map<String, BigDecimal> spendingByCategory = new HashMap<>();
        categorySpending.forEach(row -> {
            spendingByCategory.put(row[0].toString(), (BigDecimal) row[1]);
        });

        // Budget status
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(
                userId, currentMonth.getMonthValue(), currentMonth.getYear()
        );

        int activeBudgets = budgets.size();
        int budgetsAtRisk = (int) budgets.stream()
                .filter(b -> b.getCurrentSpending()
                        .compareTo(b.getMonthlyLimit().multiply(BigDecimal.valueOf(0.8))) > 0)
                .count();

        // Savings goals
        List<SavingsGoal> activeGoals = savingsGoalRepository.findByUserIdAndAchievedFalse(userId);
        BigDecimal totalSaved = activeGoals.stream()
                .map(SavingsGoal::getSavedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Upcoming bills
        List<BillReminder> upcomingBills = billReminderRepository.findUpcomingBills(
                userId, LocalDate.now(), LocalDate.now().plusDays(14)
        );

        // Top insight
        String topInsight = insightRepository.findByUserIdAndReadFalse(userId)
                .stream()
                .findFirst()
                .map(Insight::getMessage)
                .orElse("No new insights");

        return DashboardSummaryResponse.builder()
                .totalBalance(totalBalance)
                .monthlyIncome(monthlyIncome)
                .monthlyExpenses(monthlyExpenses)
                .netSavings(monthlyIncome.subtract(monthlyExpenses))
                .spendingByCategory(spendingByCategory)
                .activeBudgets(activeBudgets)
                .budgetsAtRisk(budgetsAtRisk)
                .activeSavingsGoals(activeGoals.size())
                .totalSaved(totalSaved)
                .upcomingBills(upcomingBills.size())
                .topInsight(topInsight)
                .build();
    }
}
