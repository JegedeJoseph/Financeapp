package com.financeapp.services;


import com.financeapp.dto.request.BillReminderRequest;
import com.financeapp.dto.response.BillReminderResponse;
import com.financeapp.exceptions.ResourceNotFoundException;
import com.financeapp.models.BillReminder;
import com.financeapp.models.User;
import com.financeapp.repositories.BillReminderRepository;
import com.financeapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillReminderService {

    private final BillReminderRepository billReminderRepository;
    private final UserRepository userRepository;
    private final InsightService insightService;

    @Transactional
    public BillReminderResponse createReminder(Long userId, BillReminderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BillReminder reminder = BillReminder.builder()
                .user(user)
                .billName(request.getBillName())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .recurrence(request.getRecurrence())
                .category(request.getCategory())
                .paid(false)
                .notificationSent(false)
                .build();

        BillReminder saved = billReminderRepository.save(reminder);
        return mapToResponse(saved);
    }

    public List<BillReminderResponse> getUserReminders(Long userId) {
        return billReminderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BillReminderResponse markAsPaid(Long userId, Long reminderId) {
        BillReminder reminder = billReminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill reminder not found"));

        if (!reminder.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Bill reminder not found");
        }

        reminder.setPaid(true);
        BillReminder saved = billReminderRepository.save(reminder);
        return mapToResponse(saved);
    }

    // Scheduled task to check for upcoming bills daily
    @Scheduled(cron = "0 0 9 * * *") // Run at 9 AM every day
    @Transactional
    public void checkUpcomingBills() {
        log.info("Checking for upcoming bills...");

        List<BillReminder> upcoming = billReminderRepository.findUpcomingBills(
                null, // Check all users
                LocalDate.now(),
                LocalDate.now().plusDays(3)
        );

        for (BillReminder reminder : upcoming) {
            log.info("Upcoming bill: {} due on {} for user {}",
                    reminder.getBillName(), reminder.getDueDate(), reminder.getUser().getId());
            // Could send push notifications here
        }
    }

    private BillReminderResponse mapToResponse(BillReminder reminder) {
        long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), reminder.getDueDate()
        );

        return BillReminderResponse.builder()
                .id(reminder.getId())
                .billName(reminder.getBillName())
                .amount(reminder.getAmount())
                .dueDate(reminder.getDueDate())
                .daysUntilDue(Math.max(0, daysUntilDue))
                .recurrence(reminder.getRecurrence())
                .category(reminder.getCategory())
                .paid(reminder.isPaid())
                .isOverdue(daysUntilDue < 0)
                .build();
    }
}
