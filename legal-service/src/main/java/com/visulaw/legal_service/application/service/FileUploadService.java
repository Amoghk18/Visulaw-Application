package com.visulaw.legal_service.application.service;

import com.visulaw.legal_service.application.publisher.LegalDocumentRequestPublisher;
import com.visulaw.legal_service.application.service.async.AsyncExecutorService;
import com.visulaw.legal_service.entity.DocumentMeta;
import com.visulaw.legal_service.repository.DocumentMetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final StorageService storageService;
    private final DocumentMetaRepository documentMetaRepository;
    private final AsyncExecutorService asyncExecutorService;
    private final LegalDocumentRequestPublisher publisher;

    public String uploadDocument(MultipartFile multipartFile) throws IOException {

        try {
            String s3Key = storageService.uploadFile(multipartFile);
            String userEmail = getUserEmail();
            DocumentMeta metaInfo = getMetaInfo(multipartFile.getName(), s3Key,userEmail);
            documentMetaRepository.save(metaInfo);
            DocumentMeta persistedDocument = documentMetaRepository.findByS3Key(s3Key);
            log.info("sending document request event");
            publisher.sendLegalDocumentRequest(persistedDocument);
            //asyncExecutorService.executeAsync(() -> );
            return persistedDocument.getId().toString();
        } catch (IOException e) {
            log.info("IOException while uploading the document", e);
            throw e;
        } catch (Exception e) {
            log.info("Exception while uploading the document", e);
            throw e;
        }
    }

    private DocumentMeta getMetaInfo(String name, String s3Key, String userEmail) {
        DocumentMeta metaInfo = new DocumentMeta();
        metaInfo.setFileName(name);
        metaInfo.setUploadTime(Instant.now());
        metaInfo.setS3Key(s3Key);
        metaInfo.setUserEmail(userEmail);
        return metaInfo;
    }

    private String getUserEmail() {
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            }
        }
        throw new IllegalStateException("User auth info not present");
    }

}
