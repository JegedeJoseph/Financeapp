package com.financeapp.controllers;


import com.financeapp.dto.request.BankAccountRequest;
import com.financeapp.dto.response.BankAccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/banking")
@RequiredArgsConstructor
@Tag(name = "Open Banking", description = "Open Banking integration endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class OpenBankingController {

    // Mono Connect
    @PostMapping("/mono/connect")
    @Operation(summary = "Connect bank account via Mono")
    public ResponseEntity<String> connectMonoAccount(@RequestBody String monoToken) {
        // Mock implementation
        return ResponseEntity.ok("{\"status\": \"success\", \"message\": \"Account connected via Mono\"}");
    }

    @GetMapping("/mono/accounts")
    @Operation(summary = "Get linked accounts from Mono")
    public ResponseEntity<List<String>> getMonoAccounts() {
        // Mock data
        return ResponseEntity.ok(Collections.singletonList("Mono account linked successfully"));
    }

    // Plaid Link
    @PostMapping("/plaid/connect")
    @Operation(summary = "Connect bank account via Plaid")
    public ResponseEntity<String> connectPlaidAccount(@RequestBody String plaidToken) {
        // Mock implementation
        return ResponseEntity.ok("{\"status\": \"success\", \"message\": \"Account connected via Plaid\"}");
    }

    @PostMapping("/sync")
    @Operation(summary = "Sync transactions from linked accounts")
    public ResponseEntity<String> syncTransactions() {
        // Mock sync
        return ResponseEntity.ok("{\"synced\": 25, \"message\": \"Transactions synced successfully\"}");
    }
}
