package com.visulaw.legal_service.repository;

import com.visulaw.legal_service.entity.DocumentEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentEmbeddingRepository extends JpaRepository<DocumentEmbedding, UUID>, IDocumentEmbeddingCustomRepository {

}
