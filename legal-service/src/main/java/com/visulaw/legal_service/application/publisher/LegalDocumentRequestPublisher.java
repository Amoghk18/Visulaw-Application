package com.visulaw.legal_service.application.publisher;

import com.visulaw.legal_service.entity.DocumentMeta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import visulaw.LegalService;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegalDocumentRequestPublisher {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    private static final String TOPIC = "legal-doc-requests";

    public void sendLegalDocumentRequest(DocumentMeta documentMeta) {
        log.info("Sending event {}", documentMeta);
        LegalService.LegalDocumentRequest request = getLegalDocumentRequest(documentMeta);
        CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(
                TOPIC, documentMeta.getId().toString(), request.toByteArray()
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message=[{}] with offset=[{}], distribution key: {}", request, result.getRecordMetadata().offset(), documentMeta.getId().toString());
            } else {
                log.error("Unable to send message=[{}] with distribution key: {}, due to : {}", request, documentMeta.getId().toString(), ex.getMessage());
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });
    }

    private LegalService.LegalDocumentRequest getLegalDocumentRequest(DocumentMeta documentMeta) {
        return LegalService.LegalDocumentRequest.newBuilder()
                .setId(documentMeta.getId().toString())
                .setS3Key(documentMeta.getS3Key())
                .setUploadTime(documentMeta.getUploadTime().toString())
                .setUserEmail(documentMeta.getUserEmail())
                .build();
    }

}
