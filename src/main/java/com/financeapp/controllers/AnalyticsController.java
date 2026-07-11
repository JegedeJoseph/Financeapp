package com.financeapp.controllers;


import com.financeapp.dto.response.ForecastResponse;
import com.financeapp.dto.response.SpendingPatternResponse;
import com.financeapp.services.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Financial analytics and ML insights")
@SecurityRequirement(name = "Bearer Authentication")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/forecast")
    @Operation(summary = "Get expense forecast")
    public ResponseEntity<ForecastResponse> getForecast(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "next_month") String period) {
        Long userId = getUserId(userDetails);
        ForecastResponse forecast = analyticsService.getExpenseForecast(userId, period);

        // Save forecast to history
        analyticsService.saveForecast(userId, forecast);

        return ResponseEntity.ok(forecast);
    }

    @GetMapping("/patterns")
    @Operation(summary = "Get spending patterns")
    public ResponseEntity<List<SpendingPatternResponse>> getPatterns(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(analyticsService.getSpendingPatterns(userId));
    }

    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof com.financeapp.security.UserPrincipal principal) {
            return principal.getUserId();
        }
        throw new IllegalStateException("Unexpected UserDetails type: " + userDetails.getClass());
    }
}
