package com.visulaw.legal_service.adapter.client;

import com.visulaw.legal_service.cache.CacheService;
import com.visulaw.legal_service.domain.EmbeddingRequest;
import com.visulaw.legal_service.domain.EmbeddingResponse;
import com.visulaw.legal_service.domain.QARequest;
import com.visulaw.legal_service.domain.QAResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceClientImpl implements IEmbeddingServiceClient {

    private final RestClient embeddingRestClient;
    private final CacheService cacheService;

    @Retryable(
            retryFor = { RestClientException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @Override
    public EmbeddingResponse getEmbedding(EmbeddingRequest request) {

        String cacheKey = "getEmbedding: " + DigestUtils.md5DigestAsHex(request.text().getBytes(StandardCharsets.UTF_8));
        if (cacheService.contains(cacheKey)) {
            log.info("cache hit for text: {}", request.text());
            return cacheService.get(cacheKey, EmbeddingResponse.class);
        }

        try {
            EmbeddingResponse response = embeddingRestClient.post()
                    .uri("/generate-embedding")
                    .body(request)
                    .retrieve()
                    .body(EmbeddingResponse.class);

            cacheService.put(cacheKey, response);
            return response;

        } catch (HttpStatusCodeException e) {
            log.error("HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (RestClientException e) {
            log.error("Failed to call embedding service: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error: ", e);
            throw new RuntimeException("Unexpected error while fetching embedding", e);
        }
    }

    @Retryable(
            retryFor = { RestClientException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @Override
    public QAResponse getAnswerForQuery(QARequest request) {

        String cacheKey = "getAnswerForQuery: " + "/d" + request.getDocumentId() + "/s" + request.getSessionId() + DigestUtils.md5DigestAsHex(request.getQuery().getBytes(StandardCharsets.UTF_8));
        if (cacheService.contains(cacheKey)) {
            log.info("cache hit for text: {}", request);
            return cacheService.get(cacheKey, QAResponse.class);
        }

        try {
            QAResponse response = embeddingRestClient.post()
                    .uri("/qa")
                    .body(request)
                    .retrieve()
                    .body(QAResponse.class);

            cacheService.put(cacheKey, response);
            return response;

        } catch (HttpStatusCodeException e) {
            log.error("HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (RestClientException e) {
            log.error("Failed to call embedding service: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error: ", e);
            throw new RuntimeException("Unexpected error while fetching answer fpr query", e);
        }
    }
}
