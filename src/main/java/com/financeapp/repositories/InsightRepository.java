package com.financeapp.repositories;

import com.financeapp.models.Insight;
import com.financeapp.models.enums.InsightType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsightRepository extends JpaRepository<Insight, Long> {

    Page<Insight> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Insight> findByUserIdAndReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Insight i SET i.read = true WHERE i.user.id = :userId")
    void markAllAsRead(@Param("userId") Long userId);

    @Query("SELECT COUNT(i) FROM Insight i WHERE i.user.id = :userId AND i.read = false")
    long countUnreadInsights(@Param("userId") Long userId);
}