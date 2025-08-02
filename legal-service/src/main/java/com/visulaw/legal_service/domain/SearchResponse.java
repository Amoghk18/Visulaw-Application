package com.visulaw.legal_service.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResponse {
    List<DocumentDetails> documents;
}
