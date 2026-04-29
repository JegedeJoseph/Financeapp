package com.financeapp.ml.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorizationRequest {
    private String description;
    private Double amount;
    private String merchantName;
    private String transactionDate;
}
