package com.financeapp.models;

import com.financeapp.models.enums.InsightType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "insights", indexes = {
        @Index(name = "idx_insight_user", columnList = "user_id"),
        @Index(name = "idx_insight_type", columnList = "insight_type"),
        @Index(name = "idx_insight_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Insight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InsightType insightType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private boolean read = false;

    @ElementCollection
    @CollectionTable(name = "insight_metadata",
            joinColumns = @JoinColumn(name = "insight_id"))
    @MapKeyColumn(name = "key_name")
    @Column(name = "value")
    private Map<String, String> metadata = new HashMap<>();

    @Builder.Default
    private boolean active = true;
}
