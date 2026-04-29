package com.financeapp.services;

import com.financeapp.dto.request.TransactionRequest;
import com.financeapp.dto.response.SpendingAnalyticsResponse;
import com.financeapp.dto.response.TransactionResponse;
import com.financeapp.exceptions.ResourceNotFoundException;
import com.financeapp.ml.MLServiceClient;
import com.financeapp.ml.dto.CategorizationRequest;
import com.financeapp.ml.dto.CategorizationResponse;
import com.financeapp.models.BankAccount;
import com.financeapp.models.Transaction;
import com.financeapp.models.enums.TransactionCategory;
import com.financeapp.models.enums.TransactionType;
import com.financeapp.repositories.BankAccountRepository;
import com.financeapp.repositories.BudgetRepository;
import com.financeapp.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BudgetRepository budgetRepository;
    private final MLServiceClient mlServiceClient;
    private final InsightService insightService;

    @Transactional
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        log.info("Creating transaction for user: {}", userId);

        BankAccount account = bankAccountRepository.findByIdAndUserId(request.getAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

        TransactionCategory category = request.getCategory();
        Double confidence = null;

        // If category not provided, use ML service
        if (category == null && request.getTransactionType() == TransactionType.EXPENSE) {
            try {
                CategorizationResponse mlResponse = mlServiceClient.categorizeTransaction(
                        CategorizationRequest.builder()
                                .description(request.getDescription())
                                .amount(request.getAmount().doubleValue())
                                .merchantName(request.getMerchantName())
                                .build()
                );

                category = TransactionCategory.valueOf(mlResponse.getCategory().toUpperCase());
                confidence = mlResponse.getConfidence();

                log.info("ML categorized transaction as: {} with confidence: {}", category, confidence);
            } catch (Exception e) {
                log.error("ML categorization failed, using default", e);
                category = TransactionCategory.OTHER;
            }
        } else if (category == null) {
            category = TransactionCategory.OTHER;
        }

        Transaction transaction = Transaction.builder()
                .account(account)
                .amount(request.getAmount().abs())
                .description(request.getDescription())
                .category(category)
                .transactionType(request.getTransactionType())
                .transactionDate(request.getTransactionDate())
                .merchantName(request.getMerchantName())
                .rawDescription(request.getRawDescription())
                .categoryConfidence(confidence)
                .build();

        // Update account balance
        if (request.getTransactionType() == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().subtract(request.getAmount().abs()));
        } else if (request.getTransactionType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(request.getAmount().abs()));
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        bankAccountRepository.save(account);

        // Update budget current spending
        if (request.getTransactionType() == TransactionType.EXPENSE) {
            YearMonth yearMonth = YearMonth.from(request.getTransactionDate());
            budgetRepository.incrementCurrentSpending(
                    userId, category, yearMonth.getMonthValue(), yearMonth.getYear(),
                    request.getAmount().abs()
            );

            // Check for overspending
            insightService.checkBudgetOverspending(userId, category, yearMonth);
        }

        return mapToResponse(savedTransaction);
    }

    public Page<TransactionResponse> getUserTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    public SpendingAnalyticsResponse getSpendingAnalytics(Long userId, YearMonth yearMonth) {
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal totalSpending = transactionRepository.calculateTotalSpending(
                userId, startDate, endDate);

        List<Object[]> categorySpending = transactionRepository.getCategorySpendingSummary(
                userId, startDate, endDate);

        Map<String, BigDecimal> spendingByCategory = new HashMap<>();
        categorySpending.forEach(row -> {
            spendingByCategory.put(((TransactionCategory) row[0]).name(), (BigDecimal) row[1]);
        });

        // Calculate average daily spending
        int daysInMonth = yearMonth.lengthOfMonth();
        BigDecimal avgDailySpending = totalSpending.divide(
                BigDecimal.valueOf(daysInMonth), 2, RoundingMode.HALF_UP);

        String highestCategory = spendingByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("NONE");

        return SpendingAnalyticsResponse.builder()
                .totalSpending(totalSpending)
                .averageDailySpending(avgDailySpending)
                .spendingByCategory(spendingByCategory)
                .highestSpendingCategory(highestCategory)
                .build();
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .accountName(transaction.getAccount().getAccountName())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .category(transaction.getCategory().name())
                .transactionType(transaction.getTransactionType().name())
                .transactionDate(transaction.getTransactionDate())
                .merchantName(transaction.getMerchantName())
                .categoryConfidence(transaction.getCategoryConfidence())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}