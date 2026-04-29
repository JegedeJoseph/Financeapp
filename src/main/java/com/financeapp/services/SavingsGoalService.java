package com.financeapp.services;


import com.financeapp.dto.request.SavingsGoalRequest;
import com.financeapp.dto.response.SavingsGoalResponse;
import com.financeapp.exceptions.ResourceNotFoundException;
import com.financeapp.models.SavingsGoal;
import com.financeapp.models.User;
import com.financeapp.repositories.SavingsGoalRepository;
import com.financeapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final UserRepository userRepository;
    private final InsightService insightService;

    @Transactional
    public SavingsGoalResponse createGoal(Long userId, SavingsGoalRequest request) {
        log.info("Creating savings goal for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SavingsGoal goal = SavingsGoal.builder()
                .user(user)
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .deadline(request.getDeadline())
                .description(request.getDescription())
                .savedAmount(BigDecimal.ZERO)
                .achieved(false)
                .build();

        SavingsGoal savedGoal = savingsGoalRepository.save(goal);

        return mapToResponse(savedGoal);
    }

    public List<SavingsGoalResponse> getUserGoals(Long userId, boolean includeAchieved) {
        List<SavingsGoal> goals;

        if (includeAchieved) {
            goals = savingsGoalRepository.findByUserId(userId);
        } else {
            goals = savingsGoalRepository.findByUserIdAndAchievedFalse(userId);
        }

        return goals.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SavingsGoalResponse updateProgress(Long userId, Long goalId, BigDecimal amount) {
        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));

        if (!goal.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Savings goal not found");
        }

        goal.setSavedAmount(goal.getSavedAmount().add(amount));

        if (goal.getSavedAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setAchieved(true);
            insightService.createGoalAchievedInsight(userId, goal);
        }

        SavingsGoal savedGoal = savingsGoalRepository.save(goal);

        // Check if nearly achieved for notifications
        if (!savedGoal.isAchieved() &&
                savedGoal.getSavedAmount().compareTo(savedGoal.getTargetAmount().multiply(BigDecimal.valueOf(0.9))) >= 0) {
            insightService.createGoalMilestoneInsight(userId, savedGoal);
        }

        return mapToResponse(savedGoal);
    }

    private SavingsGoalResponse mapToResponse(SavingsGoal goal) {
        BigDecimal progress = goal.getSavedAmount()
                .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), goal.getDeadline());

        return SavingsGoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .savedAmount(goal.getSavedAmount())
                .remainingAmount(goal.getTargetAmount().subtract(goal.getSavedAmount()))
                .progressPercentage(progress.doubleValue())
                .deadline(goal.getDeadline())
                .daysRemaining(Math.max(0, daysRemaining))
                .description(goal.getDescription())
                .achieved(goal.isAchieved())
                .createdAt(goal.getCreatedAt())
                .build();
    }
}
