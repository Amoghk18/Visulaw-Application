from sentence_transformers import SentenceTransformer
import logging

model = SentenceTransformer("sentence-transformers/all-mpnet-base-v2")
logger = logging.getLogger(__name__)

def generate_embeddings(text):
    logger.info("generating embeddings")
    return model.encode(text).tolist()
