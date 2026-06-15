package com.financeapp.controllers;


import com.financeapp.dto.request.BillReminderRequest;
import com.financeapp.dto.response.BillReminderResponse;
import com.financeapp.services.BillReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "Bill Reminders", description = "Bill reminder management")
@SecurityRequirement(name = "Bearer Authentication")
public class BillReminderController {

    private final BillReminderService billReminderService;

    @PostMapping
    @Operation(summary = "Create bill reminder")
    public ResponseEntity<BillReminderResponse> createReminder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BillReminderRequest request) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billReminderService.createReminder(userId, request));
    }

    @GetMapping
    @Operation(summary = "Get user's bill reminders")
    public ResponseEntity<List<BillReminderResponse>> getReminders(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(billReminderService.getUserReminders(userId));
    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "Mark bill as paid")
    public ResponseEntity<BillReminderResponse> markAsPaid(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(billReminderService.markAsPaid(userId, id));
    }

    private Long getUserId(UserDetails userDetails) {
        return 1L; // Placeholder
    }
}