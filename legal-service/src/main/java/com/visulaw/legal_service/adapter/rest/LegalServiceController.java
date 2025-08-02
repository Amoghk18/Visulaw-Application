package com.visulaw.legal_service.adapter.rest;

import com.visulaw.legal_service.application.service.EmbeddingService;
import com.visulaw.legal_service.application.service.FileUploadService;
import com.visulaw.legal_service.domain.QARequest;
import com.visulaw.legal_service.domain.QAResponse;
import com.visulaw.legal_service.domain.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/legal")
@RequiredArgsConstructor
public class LegalServiceController {

    private final FileUploadService fileUploadService;
    private final EmbeddingService embeddingService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPdf(@RequestPart("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(fileUploadService.uploadDocument(file) + " : Hold on to the request id! Your document is being processed...");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read content " + e.getMessage());
        }
    }

    @PostMapping(value = "/search")
    public ResponseEntity<SearchResponse> search(@RequestParam String query) {
       if (StringUtils.hasText(query)) {
            return new ResponseEntity<>(embeddingService.search(query), HttpStatus.OK);
       }
       return ResponseEntity.status(400).build();
    }

    @PostMapping(value = "/qa")
    public ResponseEntity<QAResponse> qaOnDocument(@RequestBody QARequest request) {
        if (isUUIDValid(request.getDocumentId()) && isUUIDValid(request.getSessionId()) && StringUtils.hasText(request.getQuery())) {
            return new ResponseEntity<>(embeddingService.getAnswerForQuery(request), HttpStatus.OK);
        }
        return ResponseEntity.status(400).build();
    }

    private boolean isUUIDValid(String id) {
        try {
            UUID.fromString(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
