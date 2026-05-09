package com.minddump.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dumps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dump {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawText;

    @Column(columnDefinition = "TEXT")
    private String doFirstJson;

    @Column(columnDefinition = "TEXT")
    private String doNextJson;

    @Column(columnDefinition = "TEXT")
    private String laterJson;

    @Column(columnDefinition = "TEXT")
    private String captureJson;

    @Column(columnDefinition = "TEXT")
    private String insight;

    @Column(columnDefinition = "TEXT")
    private String cognitiveLoadJson;

    @Column(columnDefinition = "TEXT")
    private String completedItems;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
