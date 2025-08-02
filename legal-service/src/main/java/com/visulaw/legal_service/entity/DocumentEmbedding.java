package com.visulaw.legal_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Table(name = "document_embeddings")
@Entity
@Getter
@Setter
public class DocumentEmbedding {

    @Id
    @Column(name = "document_id")
    private UUID documentId;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    private float[] embedding;

    @Column(name = "user_email")
    private String userEmail;

}
