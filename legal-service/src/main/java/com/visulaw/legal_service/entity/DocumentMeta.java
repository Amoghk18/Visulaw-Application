package com.visulaw.legal_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "document_meta")
public class DocumentMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "upload_time")
    private Instant uploadTime;

    @Column(name = "user_email")
    private String userEmail;

}
