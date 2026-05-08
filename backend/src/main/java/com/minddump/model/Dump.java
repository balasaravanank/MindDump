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
    private String urgent;

    @Column(columnDefinition = "TEXT")
    private String thisWeek;

    @Column(columnDefinition = "TEXT")
    private String someday;

    @Column(columnDefinition = "TEXT")
    private String ideas;

    @Column(columnDefinition = "TEXT")
    private String insight;

    @Column(columnDefinition = "TEXT")
    private String completedItems;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
