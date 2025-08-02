import json
from app.redis import redis_client

async def get_chat_history(session_id: str):
    raw = await redis_client.get(f"chat:{session_id}")
    return json.loads(raw) if raw else []

async def update_chat_history(session_id: str, question: str, answer: str):
    history = await get_chat_history(session_id)
    history.append({"question": question, "answer": answer})
    redis_client.set(f"chat:{session_id}", json.dumps(history), ex=3600)
