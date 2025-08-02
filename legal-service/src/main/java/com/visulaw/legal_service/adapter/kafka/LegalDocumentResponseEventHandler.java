package com.visulaw.legal_service.adapter.kafka;

import com.visulaw.legal_service.application.service.LegalDocumentService;
import com.visulaw.legal_service.domain.LegalDocumentEmbeddingsDto;
import com.visulaw.legal_service.domain.RiskyClause;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import visulaw.LegalService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegalDocumentResponseEventHandler {

    private final LegalDocumentService legalDocumentService;

    @KafkaListener(
            topics = "legal-doc-embedding-responses",
            groupId = "legal-service-consumer"
    )
    public void consume(byte[] messageBytes) {
        try {
            LegalService.LegalDocumentResponse response = LegalService.LegalDocumentResponse.parseFrom(messageBytes);

            log.info("Document ID: {}", response.getId());
            log.info("Timestamp: {}", response.getTimestamp());
            log.info("Embeddings: {}", response.getEmbeddingsList());
            log.info("User email: {}", response.getEmail());
            log.info("text: {}", response.getText());
            log.info("clauses : {}", response.getClausesList());

            legalDocumentService.saveEmbeddings(protoToDomain(response));

        } catch (Exception e) {
            log.error("Failed to parse protobuf message: {}", e.getMessage(), e);
        }
    }

    private LegalDocumentEmbeddingsDto protoToDomain(LegalService.LegalDocumentResponse response) {
        LegalDocumentEmbeddingsDto dto = new LegalDocumentEmbeddingsDto();
        dto.setDocumentId(response.getId());
        dto.setTimestamp(response.getTimestamp());
        dto.setEmbeddings(response.getEmbeddingsList());
        dto.setUserEmail(response.getEmail());
        dto.setText(response.getText());
        dto.setClauses(getRiskyClauses(response.getClausesList()));
        return dto;
    }

    private List<RiskyClause> getRiskyClauses(List<LegalService.RiskyClauseDetails> clausesList) {
        return clausesList.stream().map(clause ->
            RiskyClause.builder()
                    .clause(clause.getClause())
                    .tag(clause.getTag())
                    .confidence(clause.getConfidence())
                    .startIndex(clause.getStartIndex())
                    .endIndex(clause.getEndIndex())
                    .riskScore(clause.getRiskScore())
                    .build()
        ).toList();
    }
}
