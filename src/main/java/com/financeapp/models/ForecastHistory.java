package com.financeapp.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "forecast_history", indexes = {
        @Index(name = "idx_forecast_user", columnList = "user_id"),
        @Index(name = "idx_forecast_period", columnList = "forecast_period")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "forecast_period", nullable = false)
    private String forecastPeriod; // e.g., "2026-06"

    @Column(name = "forecast_month", nullable = false)
    private Integer month;

    @Column(name = "forecast_year", nullable = false)
    private Integer year;

    @Column(name = "predicted_total", precision = 19, scale = 4)
    private BigDecimal predictedTotal;

    @Column(columnDefinition = "TEXT")
    private String categoryPredictions; // JSON string of category-wise predictions

    private Double confidence;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
