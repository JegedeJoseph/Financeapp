package com.financeapp.dto.response;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingPatternResponse {
    private String patternType;
    private String description;
    private Map<String, Object> details;
    private List<String> recommendations;
}
