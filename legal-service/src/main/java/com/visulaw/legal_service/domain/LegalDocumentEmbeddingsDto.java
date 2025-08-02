package com.visulaw.legal_service.domain;

import lombok.Data;

import java.util.List;

@Data
public class LegalDocumentEmbeddingsDto {
    private String documentId;
    private String timestamp;
    private List<Float> embeddings;
    private String userEmail;
    private String text;
    private List<RiskyClause> clauses;
}
