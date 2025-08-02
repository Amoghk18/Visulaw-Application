from aiokafka import AIOKafkaProducer
from app.config import settings
import logging
import time
from generated.embedding_pb2 import LegalDocumentResponse

producer = None
logger = logging.getLogger(__name__)

async def init_producer():
    global producer
    producer = AIOKafkaProducer(bootstrap_servers=settings.kafka_bootstrap_servers)
    await producer.start()

async def send_embedding_event(document_id, embedding, user_email, text, clauses):
    if not producer:
        await init_producer()

    event = LegalDocumentResponse(
        id = document_id,
        embeddings = embedding,
        timestamp = str(int(time.time())),
        email = user_email,
        text = text,
        clauses = clauses
    )

    await producer.send_and_wait(
        settings.kafka_output_topic,
        event.SerializeToString()
    )
