package com.financeapp.controllers;


import com.financeapp.dto.response.DashboardSummaryResponse;
import com.financeapp.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard overview endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        // Extract user ID from authentication (implementation depends on your setup)
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(dashboardService.getDashboardSummary(userId));
    }

    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof com.financeapp.security.UserPrincipal principal) {
            return principal.getUserId();
        }
        throw new IllegalStateException("Unexpected UserDetails type: " + userDetails.getClass());
    }
}
