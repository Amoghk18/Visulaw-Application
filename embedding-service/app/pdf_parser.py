import fitz  # PyMuPDF
import logging

logger = logging.getLogger(__name__)

def extract_text(pdf_path):
    logger.info("extracting text")
    doc = fitz.open(pdf_path)
    return "\n".join([page.get_text() for page in doc])
