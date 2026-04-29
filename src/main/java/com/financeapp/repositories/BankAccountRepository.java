package com.financeapp.repositories;

import com.financeapp.models.BankAccount;
import com.financeapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByUserId(Long userId);

    List<BankAccount> findByUserIdAndActiveTrue(Long userId);

    Optional<BankAccount> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(b.balance), 0) FROM BankAccount b WHERE b.user.id = :userId AND b.active = true")
    BigDecimal calculateTotalBalanceByUser(@Param("userId") Long userId);
}
