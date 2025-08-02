from pydantic import BaseModel

class QARequest(BaseModel):
    query: str
    documentId: str
    sessionId: str

class EmbeddingRequest(BaseModel):
    text: str

class EmbeddingResponse(BaseModel):
    embedding: list[float]