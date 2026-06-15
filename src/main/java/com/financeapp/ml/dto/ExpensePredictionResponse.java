package com.financeapp.ml.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpensePredictionResponse {
    private Map<String, Double> predictedExpenses;
    private Double totalPredicted;
    private Double confidence;
    private String recommendation;
}
