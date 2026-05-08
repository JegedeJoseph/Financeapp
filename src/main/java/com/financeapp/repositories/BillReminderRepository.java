package com.financeapp.repositories;

import com.financeapp.models.BillReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillReminderRepository extends JpaRepository<BillReminder, Long> {

    List<BillReminder> findByUserId(Long userId);

    List<BillReminder> findByUserIdAndPaidFalse(Long userId);

    @Query("SELECT br FROM BillReminder br WHERE br.user.id = :userId " +
            "AND br.dueDate BETWEEN :startDate AND :endDate " +
            "AND br.paid = false AND br.notificationSent = false")
    List<BillReminder> findUpcomingBills(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
}