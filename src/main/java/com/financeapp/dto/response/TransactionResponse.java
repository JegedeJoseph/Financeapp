package com.financeapp.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private Long accountId;
    private String accountName;
    private BigDecimal amount;
    private String description;
    private String category;
    private String transactionType;
    private LocalDateTime transactionDate;
    private String merchantName;
    private Double categoryConfidence;
    private LocalDateTime createdAt;

    @Builder.Default
    private boolean active = true;
}