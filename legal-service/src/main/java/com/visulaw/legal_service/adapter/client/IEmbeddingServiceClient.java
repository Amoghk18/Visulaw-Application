package com.visulaw.legal_service.adapter.client;

import com.visulaw.legal_service.domain.EmbeddingRequest;
import com.visulaw.legal_service.domain.EmbeddingResponse;
import com.visulaw.legal_service.domain.QARequest;
import com.visulaw.legal_service.domain.QAResponse;

public interface IEmbeddingServiceClient {

    EmbeddingResponse getEmbedding(EmbeddingRequest request);

    QAResponse getAnswerForQuery(QARequest request);

}
