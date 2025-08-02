from app.models import QARequest
from app.qa.history import get_chat_history, update_chat_history
from app.qa.longformer import generate_answer
from app.s3_utils import download_pdf_from_r2
from app.pdf_parser import extract_text
import logging
from app.redis import redis_client

logger = logging.getLogger(__name__)

def prepare_context(document: str, history: list, limit = 6) -> str:
    prev = [f"Q: {h['question']} A: {h['answer']}" for h in history[-limit:]]
    return "\n".join(prev + [document])

async def handle_qa(request: QARequest):

    doc_key = f"doc_text:{request.documentId}"

    document_text = await redis_client.get(doc_key)
    if not document_text:
        logger.info(f"getting document text for first time query on document {doc_key}")
        document_text = await fetch_document_text(request.documentId)
        await redis_client.set(doc_key, document_text)

    logger.info("handling request, getting history")
    history = await get_chat_history(request.sessionId)
    logger.info("preparing context")
    context = prepare_context(document_text, history)
    logger.info("generating answer")
    answer = generate_answer(request.query, context)
    await update_chat_history(request.sessionId, request.query, answer)
    return {"answer": answer}

async def fetch_document_text(documentId: str):
    local_path = download_pdf_from_r2(documentId)
    text = extract_text(local_path)
    return text