CREATE TABLE document_embeddings (
    document_id UUID PRIMARY KEY,
    user_email varchar,
    embedding vector(768),
    created_at TIMESTAMP
);