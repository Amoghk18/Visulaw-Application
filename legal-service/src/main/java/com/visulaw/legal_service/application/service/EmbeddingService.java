package com.visulaw.legal_service.application.service;

import com.visulaw.legal_service.adapter.client.IEmbeddingServiceClient;
import com.visulaw.legal_service.domain.*;
import com.visulaw.legal_service.entity.DocumentMeta;
import com.visulaw.legal_service.exceptions.NoDocumentFoundException;
import com.visulaw.legal_service.repository.DocumentEmbeddingRepository;
import com.visulaw.legal_service.repository.DocumentMetaRepository;
import com.visulaw.legal_service.util.DateTimeUtils;
import com.visulaw.legal_service.util.EmbeddingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final IEmbeddingServiceClient embeddingServiceClient;
    private final DocumentEmbeddingRepository documentEmbeddingRepository;
    private final DocumentMetaRepository documentMetaRepository;

    public SearchResponse search(String query) {
        EmbeddingResponse response = embeddingServiceClient.getEmbedding(new EmbeddingRequest(query));
        List<Object[]> rows = documentEmbeddingRepository.findTopSimilarEmbeddings(
                EmbeddingUtils.convertListToArray(response.embedding()), 5
        );
        List<DocumentDetails> documentDetails = rows.stream().map(this::mapToDocumentDetails).toList();
        return new SearchResponse(documentDetails);
    }

    public QAResponse getAnswerForQuery(QARequest request) {
        Optional<DocumentMeta> documentMeta = documentMetaRepository.findById(UUID.fromString(request.getDocumentId()));
        if (documentMeta.isPresent()) {
            request.setDocumentId(documentMeta.get().getS3Key());
            return embeddingServiceClient.getAnswerForQuery(request);
        }
        throw new NoDocumentFoundException("No document found with id" + request.getDocumentId());
    }

    private DocumentDetails mapToDocumentDetails(Object[] row) {
        return DocumentDetails.builder()
                .documentId((UUID) row[0])
                .createdBy((String) row[1])
                .createdAt(DateTimeUtils.timeStampToString((Timestamp) row[2]))
                .build();
    }

}
