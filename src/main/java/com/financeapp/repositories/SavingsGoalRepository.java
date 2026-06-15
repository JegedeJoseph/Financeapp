package com.financeapp.repositories;

import com.financeapp.models.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUserId(Long userId);

    List<SavingsGoal> findByUserIdAndAchievedFalse(Long userId);

    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.user.id = :userId " +
            "AND sg.achieved = false AND sg.savedAmount >= (sg.targetAmount * 0.9)")
    List<SavingsGoal> findNearlyAchievedGoals(@Param("userId") Long userId);

    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.user.id = :userId " +
            "AND sg.deadline < CURRENT_DATE AND sg.achieved = false")
    List<SavingsGoal> findOverdueGoals(@Param("userId") Long userId);
}
