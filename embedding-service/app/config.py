from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    kafka_bootstrap_servers: str = "kafka:9092"
    kafka_input_topic: str = "legal-doc-requests"
    kafka_output_topic: str = "legal-doc-embedding-responses"
    r2_endpoint: str
    r2_key: str
    r2_secret: str
    r2_bucket: str
    api_key: str

    class Config:
        env_file = ".env"

settings = Settings()
