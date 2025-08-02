package com.visulaw.legal_service.application.service;

import com.visulaw.legal_service.domain.LegalDocumentEmbeddingsDto;
import com.visulaw.legal_service.domain.RiskyClause;
import com.visulaw.legal_service.entity.Clause;
import com.visulaw.legal_service.entity.DocumentEmbedding;
import com.visulaw.legal_service.repository.ClauseRepository;
import com.visulaw.legal_service.repository.DocumentEmbeddingRepository;
import com.visulaw.legal_service.util.EmbeddingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegalDocumentService {

    private final DocumentEmbeddingRepository documentEmbeddingRepository;
    private final ClauseRepository clauseRepository;

    @Transactional
    public void saveEmbeddings(LegalDocumentEmbeddingsDto legalDocumentEmbeddingsDto) {
        log.info("saving embeddings");
        UUID documentId = UUID.fromString(legalDocumentEmbeddingsDto.getDocumentId());
        try {
            documentEmbeddingRepository.insertEmbedding(
                    documentId,
                    legalDocumentEmbeddingsDto.getUserEmail(),
                    EmbeddingUtils.convertListToArray(legalDocumentEmbeddingsDto.getEmbeddings()),
                    Instant.ofEpochSecond(Long.parseLong(legalDocumentEmbeddingsDto.getTimestamp()))
            );
            clauseRepository.saveAll(dtoToEntity(legalDocumentEmbeddingsDto.getClauses(), documentId));

        } catch (Exception e) {
            log.error("Embeddings failed to be stored with exception {}", e.getMessage(), e);
            throw e;
        }
    }

    private List<Clause> dtoToEntity(List<RiskyClause> clauses, UUID documentId) {
        return clauses.stream().map(clauseDto -> {
            Clause clause = new Clause();
            clause.setDocumentId(documentId);
            clause.setClause(clauseDto.getClause());
            clause.setTag(clauseDto.getTag());
            clause.setConfidence(Double.parseDouble(clauseDto.getConfidence()));
            clause.setStartIdx(Integer.parseInt(clauseDto.getStartIndex()));
            clause.setEndIdx(Integer.parseInt(clauseDto.getEndIndex()));
            clause.setRiskScore(Double.parseDouble(clauseDto.getRiskScore()));
            return clause;
        }).toList();
    }

}
