package com.financeapp.controllers;


import com.financeapp.dto.request.TransactionRequest;
import com.financeapp.dto.response.SpendingAnalyticsResponse;
import com.financeapp.dto.response.TransactionResponse;
import com.financeapp.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Create a new transaction")
    public ResponseEntity<TransactionResponse> createTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionRequest request) {

        Long userId = getUserIdFromUserDetails(userDetails);
        TransactionResponse response = transactionService.createTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get user transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        Long userId = getUserIdFromUserDetails(userDetails);
        Page<TransactionResponse> transactions = transactionService.getUserTransactions(userId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get spending analytics")
    public ResponseEntity<SpendingAnalyticsResponse> getSpendingAnalytics(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {

        Long userId = getUserIdFromUserDetails(userDetails);
        SpendingAnalyticsResponse analytics = transactionService.getSpendingAnalytics(userId, yearMonth);
        return ResponseEntity.ok(analytics);
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails instanceof com.financeapp.security.UserPrincipal principal) {
            return principal.getUserId();
        }
        throw new IllegalStateException("Unexpected UserDetails type: " + userDetails.getClass());
    }
}