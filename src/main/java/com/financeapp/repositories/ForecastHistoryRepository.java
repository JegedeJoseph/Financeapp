package com.financeapp.repositories;

import com.financeapp.models.ForecastHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForecastHistoryRepository extends JpaRepository<ForecastHistory, Long> {

    List<ForecastHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ForecastHistory> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);
}