from aiokafka import AIOKafkaConsumer
from generated.embedding_pb2 import LegalDocumentRequest
from app.s3_utils import download_pdf_from_r2
from app.pdf_parser import extract_text
from app.embedding_generator import generate_embeddings
from app.kafka_producer import send_embedding_event
from app.clause_tagging import analyze_risky_clauses
from app.config import settings
import asyncio
import logging

logger = logging.getLogger(__name__)

# needed as there is no guarantee that topic will exist before embedding service spins up
async def create_kafka_consumer_with_retries(max_retries=10, delay_seconds=5):
    for attempt in range(max_retries):
        try:
            consumer = AIOKafkaConsumer(
                settings.kafka_input_topic,
                bootstrap_servers=settings.kafka_bootstrap_servers,
                group_id="embedding-service",
                value_deserializer=lambda m: m
            )
            await consumer.start()
            logger.info("Kafka consumer started successfully.")
            print("Kafka consumer started successfully.")
            return consumer
        except Exception as e:
            logger.warning(f"Kafka not ready (attempt {attempt + 1}/{max_retries}): {e}")
            await asyncio.sleep(delay_seconds)
    raise RuntimeError("Kafka not available after retries")


async def start_kafka_listener():
    consumer = await create_kafka_consumer_with_retries()

    try:
        async for msg in consumer:
            try:
                request = LegalDocumentRequest()
                request.ParseFromString(msg.value)

                document_id = request.id
                key = request.s3key
                user_email = request.user_email

                local_path = download_pdf_from_r2(key)
                text = extract_text(local_path)
                embedding = generate_embeddings(text)
                clauses = await analyze_risky_clauses(text = text)

                logger.info(f"local path: {local_path}")
                logger.info(f"extracted text: {text}")
                logger.info(f"embeddings: {embedding}")

                await send_embedding_event(document_id, embedding, user_email, text, clauses)

            except Exception as e:
                logger.error(f"Failed to process message: {e}")

    finally:
        await consumer.stop()
        logger.info("Kafka consumer stopped.")
