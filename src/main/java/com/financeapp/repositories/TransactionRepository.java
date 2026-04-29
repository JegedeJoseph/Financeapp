package com.financeapp.repositories;

import com.financeapp.models.Transaction;
import com.financeapp.models.enums.TransactionCategory;
import com.financeapp.models.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.account.user.id = :userId ORDER BY t.transactionDate DESC")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT t FROM Transaction t WHERE t.account.user.id = :userId " +
            "AND t.category = :category ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndCategory(@Param("userId") Long userId,
                                              @Param("category") TransactionCategory category);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.account.user.id = :userId AND t.transactionType = 'EXPENSE' " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalSpending(@Param("userId") Long userId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.account.user.id = :userId AND t.transactionType = 'EXPENSE' " +
            "AND t.category = :category AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateCategorySpending(@Param("userId") Long userId,
                                         @Param("category") TransactionCategory category,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0) as total " +
            "FROM Transaction t WHERE t.account.user.id = :userId " +
            "AND t.transactionType = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY t.category ORDER BY total DESC")
    List<Object[]> getCategorySpendingSummary(@Param("userId") Long userId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    List<Transaction> findByAccountUserIdAndTransactionDateBetween(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
