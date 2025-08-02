package com.visulaw.legal_service.domain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DocumentDetails {

    private UUID documentId;
    private String createdBy;
    private String createdAt;

}
