package com.financeapp.services;

import com.financeapp.dto.request.BudgetRequest;
import com.financeapp.dto.response.BudgetResponse;
import com.financeapp.exceptions.ResourceAlreadyExistsException;
import com.financeapp.exceptions.ResourceNotFoundException;
import com.financeapp.models.Budget;
import com.financeapp.models.User;
import com.financeapp.models.enums.TransactionCategory;
import com.financeapp.repositories.BudgetRepository;
import com.financeapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @Transactional
    public BudgetResponse createBudget(Long userId, BudgetRequest request) {
        log.info("Creating budget for user: {}, category: {}", userId, request.getCategory());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if budget already exists
        budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                        userId, request.getCategory(), request.getMonth(), request.getYear())
                .ifPresent(b -> {
                    throw new ResourceAlreadyExistsException("Budget already exists for this period");
                });

        Budget budget = Budget.builder()
                .user(user)
                .category(request.getCategory())
                .monthlyLimit(request.getMonthlyLimit())
                .month(request.getMonth())
                .year(request.getYear())
                .currentSpending(java.math.BigDecimal.ZERO)
                .build();

        Budget savedBudget = budgetRepository.save(budget);

        return mapToResponse(savedBudget);
    }

    public List<BudgetResponse> getUserBudgets(Long userId, Integer month, Integer year) {
        List<Budget> budgets;

        if (month != null && year != null) {
            budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        } else {
            budgets = budgetRepository.findByUserId(userId);
        }

        return budgets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BudgetResponse updateBudget(Long userId, Long budgetId, BudgetRequest request) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        if (!budget.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Budget not found");
        }

        budget.setMonthlyLimit(request.getMonthlyLimit());

        Budget updatedBudget = budgetRepository.save(budget);

        return mapToResponse(updatedBudget);
    }

    private BudgetResponse mapToResponse(Budget budget) {
        java.math.BigDecimal remaining = budget.getMonthlyLimit().subtract(budget.getCurrentSpending());
        double percentageUsed = budget.getCurrentSpending()
                .divide(budget.getMonthlyLimit(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal.valueOf(100))
                .doubleValue();

        return BudgetResponse.builder()
                .id(budget.getId())
                .category(budget.getCategory().name())
                .monthlyLimit(budget.getMonthlyLimit())
                .currentSpending(budget.getCurrentSpending())
                .remaining(remaining)
                .percentageUsed(percentageUsed)
                .month(budget.getMonth())
                .year(budget.getYear())
                .overspent(budget.getCurrentSpending().compareTo(budget.getMonthlyLimit()) > 0)
                .build();
    }
}
