package com.visulaw.legal_service.repository;

import com.visulaw.legal_service.entity.DocumentEmbedding;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDocumentEmbeddingCustomRepository {

    void insertEmbedding(UUID documentId, String userEmail, float[] embedding, Instant createdAt);
    List<Object[]> findTopSimilarEmbeddings(float[] queryEmbedding, int limit);
}
