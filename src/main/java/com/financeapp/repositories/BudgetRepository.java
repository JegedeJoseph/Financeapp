package com.financeapp.repositories;

import com.financeapp.models.Budget;
import com.financeapp.models.User;
import com.financeapp.models.enums.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);

    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(
            Long userId, TransactionCategory category, Integer month, Integer year);

    @Modifying
    @Query("UPDATE Budget b SET b.currentSpending = b.currentSpending + :amount " +
            "WHERE b.user.id = :userId AND b.category = :category " +
            "AND b.month = :month AND b.year = :year")
    void incrementCurrentSpending(@Param("userId") Long userId,
                                  @Param("category") TransactionCategory category,
                                  @Param("month") Integer month,
                                  @Param("year") Integer year,
                                  @Param("amount") BigDecimal amount);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
            "AND b.month = :month AND b.year = :year " +
            "AND b.currentSpending > b.monthlyLimit AND b.alertSent = false")
    List<Budget> findOverspentBudgets(@Param("userId") Long userId,
                                      @Param("month") Integer month,
                                      @Param("year") Integer year);
}
