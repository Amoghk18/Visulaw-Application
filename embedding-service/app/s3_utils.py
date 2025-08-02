import boto3
from app.config import settings
import logging

logger = logging.getLogger(__name__)

def download_pdf_from_r2(key):
    s3 = boto3.client(
        's3',
        endpoint_url=settings.r2_endpoint,
        aws_access_key_id=settings.r2_key,
        aws_secret_access_key=settings.r2_secret
    )

    logger.info(f"s3key: {key}")

    local_path = f"/tmp/{key.split('/')[-1]}"
    logger.info(f"path: {key}")
    s3.download_file(settings.r2_bucket, key, local_path)
    return local_path
