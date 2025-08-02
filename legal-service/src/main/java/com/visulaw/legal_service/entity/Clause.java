package com.visulaw.legal_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "risky_clauses")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Clause {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_id")
    private UUID documentId;

    @Column(name = "clause_text", columnDefinition = "TEXT")
    private String clause;

    @Column(name = "tag")
    private String tag;

    @Column(name = "confidence")
    private double confidence;

    @Column(name = "start_idx")
    private int startIdx;

    @Column(name = "end_idx")
    private int endIdx;

    @Column(name = "risk_score")
    private double riskScore;

}
