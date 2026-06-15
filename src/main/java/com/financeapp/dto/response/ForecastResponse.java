package com.financeapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResponse {
    private String period;
    private BigDecimal predictedTotal;
    private Map<String, BigDecimal> categoryPredictions;
    private Double confidence;
    private String recommendation;
}
