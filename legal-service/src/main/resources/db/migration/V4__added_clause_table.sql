CREATE TABLE IF NOT EXISTS risky_clauses (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    clause_text text NOT NULL,
    tag VARCHAR NOT NULL,
    confidence DOUBLE PRECISION,
    start_idx INT,
    end_idx INT,
    risk_score DOUBLE PRECISION,
    CONSTRAINT fk_document_embedding
        FOREIGN KEY (document_id)
        REFERENCES document_embeddings(document_id)
        ON DELETE CASCADE
);