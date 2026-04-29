package com.financeapp.ml.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorizationResponse {
    private String category;
    private Double confidence;
    private String subcategory;
}