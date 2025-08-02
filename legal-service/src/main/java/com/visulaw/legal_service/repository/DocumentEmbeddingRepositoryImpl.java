package com.visulaw.legal_service.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public class DocumentEmbeddingRepositoryImpl implements IDocumentEmbeddingCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void insertEmbedding(UUID documentId, String userEmail, float[] embedding, Instant createdAt) {
        String vectorStr = Arrays.toString(embedding);

        String sql = """
            INSERT INTO document_embeddings (document_id, user_email, embedding, created_at)
            VALUES (:documentId, :userEmail, CAST(:embedding AS vector), :createdAt)
        """;

        entityManager.createNativeQuery(sql)
                .setParameter("documentId", documentId)
                .setParameter("userEmail", userEmail)
                .setParameter("embedding", vectorStr)
                .setParameter("createdAt", Timestamp.from(createdAt))
                .executeUpdate();
        entityManager.flush();
    }

    @Override
    public List<Object[]> findTopSimilarEmbeddings(float[] queryEmbedding, int limit) {
        String vectorStr = Arrays.toString(queryEmbedding);

        String sql = """
            SELECT document_id, user_email, created_at, embedding
            FROM document_embeddings
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
        """;

        return entityManager.createNativeQuery(sql)
                .setParameter("embedding", vectorStr)
                .setParameter("limit", limit)
                .getResultList();
    }
}
