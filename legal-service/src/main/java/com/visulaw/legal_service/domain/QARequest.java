package com.visulaw.legal_service.domain;

import lombok.Data;

@Data
public class QARequest {
    private String documentId;
    private String sessionId;
    private String query;
}
