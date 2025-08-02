package com.visulaw.legal_service.repository;

import com.visulaw.legal_service.entity.DocumentMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentMetaRepository extends JpaRepository<DocumentMeta, UUID> {

    DocumentMeta findByS3Key(String s3key);

}
