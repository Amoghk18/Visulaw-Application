from transformers import LongformerTokenizer, LongformerForQuestionAnswering
import torch

tokenizer = LongformerTokenizer.from_pretrained("allenai/longformer-base-4096")
model = LongformerForQuestionAnswering.from_pretrained("allenai/longformer-base-4096")
model.eval()

def generate_answer(question: str, context: str) -> str:
    inputs = tokenizer.encode_plus(question, context, return_tensors="pt", max_length=4096, truncation=True)
    with torch.no_grad():
        outputs = model(**inputs)

    start = torch.argmax(outputs.start_logits)
    end = torch.argmax(outputs.end_logits) + 1
    answer_tokens = inputs["input_ids"][0][start:end]
    return tokenizer.decode(answer_tokens, skip_special_tokens=True)
