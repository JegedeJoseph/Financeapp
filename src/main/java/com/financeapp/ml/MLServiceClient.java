package com.financeapp.ml;


import com.financeapp.ml.dto.CategorizationRequest;
import com.financeapp.ml.dto.CategorizationResponse;
import com.financeapp.ml.dto.ExpensePredictionResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MLServiceClient {

    private final WebClient webClient;

    @Value("${ml.service.url:http://localhost:5000}")
    private String mlServiceUrl;

    @Value("${ml.service.api-key:}")
    private String apiKey;

    public MLServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(mlServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @CircuitBreaker(name = "mlService", fallbackMethod = "categorizeTransactionFallback")
    @Retry(name = "mlService", fallbackMethod = "categorizeTransactionFallback")
    public CategorizationResponse categorizeTransaction(CategorizationRequest request) {
        log.info("Calling ML service for transaction categorization: {}", request.getDescription());

        try {
            return webClient.post()
                    .uri("/api/v1/ml/categorize-transaction")
                    .header("X-API-Key", apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CategorizationResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

        } catch (Exception e) {
            log.error("Error calling ML categorization service", e);
            return categorizeTransactionFallback(request, e);
        }
    }

    private CategorizationResponse categorizeTransactionFallback(CategorizationRequest request, Throwable t) {
        log.warn("Using fallback categorization for: {}", request.getDescription());

        // Simple rule-based fallback
        String category = ruleBasedCategorization(request.getDescription().toLowerCase());

        return CategorizationResponse.builder()
                .category(category)
                .confidence(0.5)
                .build();
    }

    @CircuitBreaker(name = "mlService", fallbackMethod = "predictExpensesFallback")
    public ExpensePredictionResponse predictExpenses(Long userId, String period) {
        log.info("Calling ML service for expense prediction for user: {}", userId);

        Map<String, Object> request = Map.of(
                "userId", userId,
                "period", period
        );

        try {
            return webClient.post()
                    .uri("/api/v1/ml/predict-expense")
                    .header("X-API-Key", apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ExpensePredictionResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

        } catch (Exception e) {
            log.error("Error calling ML prediction service", e);
            return predictExpensesFallback(userId, period, e);
        }
    }

    private ExpensePredictionResponse predictExpensesFallback(Long userId, String period, Throwable t) {
        log.warn("Using fallback expense prediction");

        Map<String, Double> predictions = new HashMap<>();
        predictions.put("GROCERIES", 500.0);
        predictions.put("TRANSPORTATION", 200.0);
        predictions.put("ENTERTAINMENT", 150.0);

        return ExpensePredictionResponse.builder()
                .predictedExpenses(predictions)
                .totalPredicted(850.0)
                .confidence(0.3)
                .recommendation("Based on historical averages")
                .build();
    }

    private String ruleBasedCategorization(String description) {
        if (description.contains("restaurant") || description.contains("cafe") ||
                description.contains("mcdonald") || description.contains("starbucks")) {
            return "DINING_OUT";
        } else if (description.contains("grocery") || description.contains("supermarket") ||
                description.contains("shoprite") || description.contains("walmart")) {
            return "GROCERIES";
        } else if (description.contains("uber") || description.contains("lyft") ||
                description.contains("gas") || description.contains("fuel")) {
            return "TRANSPORTATION";
        } else if (description.contains("amazon") || description.contains("ebay") ||
                description.contains("target")) {
            return "SHOPPING";
        } else if (description.contains("netflix") || description.contains("spotify") ||
                description.contains("cinema")) {
            return "ENTERTAINMENT";
        } else {
            return "OTHER";
        }
    }
}