from fastapi import FastAPI, APIRouter, Depends
import asyncio
import logging

from app.kafka_consumer import start_kafka_listener
from app.models import QARequest, EmbeddingRequest, EmbeddingResponse
from app.qa.service import handle_qa
from app.auth.auth import verify_api_key
from app.embedding_generator import generate_embeddings

logging.basicConfig(
    level = logging.INFO,
    format = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)

app = FastAPI()

router = APIRouter(prefix = "/embedding")


@app.on_event("startup")
async def startup_event():
    asyncio.create_task(start_kafka_listener())

@app.get("/health")
def health_check():
    return {"status": "ok"}

@router.post("/qa", dependencies=[Depends(verify_api_key)])
async def qa(request: QARequest):
    return await handle_qa(request)


@router.post("/generate-embedding", dependencies=[Depends(verify_api_key)], response_model=EmbeddingResponse)
def generate_embedding(request: EmbeddingRequest):
    embedding = generate_embeddings(request.text)
    return EmbeddingResponse(embedding=embedding)

app.include_router(router)