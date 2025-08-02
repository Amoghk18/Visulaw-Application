from transformers import pipeline, AutoModelForSequenceClassification, AutoTokenizer
from fastapi import HTTPException
import torch
import asyncio
import re
import nltk
nltk.download('punkt')
from nltk.tokenize import PunktSentenceTokenizer
from nltk.tokenize.punkt import PunktParameters
from generated.embedding_pb2 import RiskyClauseDetails
from typing import List, Optional, Dict, Any
import logging

logger = logging.getLogger(__name__)

RISK_WEIGHTS = {
"Uncapped_Liability": 0.95, # Very high risk
"Anti_Assignment": 0.88, # High risk
"Covenant_Not_To_Sue": 0.85, # High risk
"Cap_On_Liability": 0.75, # Medium-high risk
"Notice_Period_To_Terminate_Renewal": 0.65, # Medium risk
"Change_Of_Control": 0.60, # Medium risk
"Non_Compete": 0.58, # Medium risk
"Exclusivity": 0.55, # Medium risk
"Post_Termination_Services": 0.50, # Medium risk
"Termination_For_Convenience": 0.45, # Medium-low risk
"Insurance": 0.40, # Low-medium risk
"Warranty_Duration": 0.35, # Low-medium risk
"Audit_Rights": 0.30, # Low risk
"Parties": 0.20, # Very low risk
"Agreement_Date": 0.15, # Very low risk
"Document_Name": 0.10, # Very low risk
}

CUAD_LABELS = {
0: "Document_Name", 1: "Parties", 2: "Agreement_Date", 3: "Effective_Date",
4: "Expiration_Date", 5: "Renewal_Term", 6: "Notice_Period_To_Terminate_Renewal",
7: "Governing_Law", 8: "Most_Favored_Nation", 9: "Non_Compete",
10: "Exclusivity", 11: "No_Solicit_Of_Customers", 12: "Competitive_Restriction_Exception",
13: "No_Solicit_Of_Employees", 14: "Non_Disparagement", 15: "Termination_For_Convenience",
16: "Rofr_Rofo_Rofn", 17: "Change_Of_Control", 18: "Anti_Assignment",
19: "Revenue_Profit_Sharing", 20: "Price_Restriction", 21: "Minimum_Commitment",
22: "Volume_Restriction", 23: "Ip_Ownership_Assignment", 24: "Joint_Ip_Ownership",
25: "License_Grant", 26: "Non_Transferable_License", 27: "Affiliate_License_Licensor",
28: "Affiliate_License_Licensee", 29: "Unlimited_All_You_Can_Eat_License",
30: "Irrevocable_Or_Perpetual_License", 31: "Source_Code_Escrow",
32: "Post_Termination_Services", 33: "Audit_Rights", 34: "Uncapped_Liability",
35: "Cap_On_Liability", 36: "Liquidated_Damages", 37: "Warranty_Duration",
38: "Insurance", 39: "Covenant_Not_To_Sue", 40: "Third_Party_Beneficiary"
}

ml_models = {}
model_name = "nlpaueb/legal-bert-base-uncased"  # Superior for legal text
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModelForSequenceClassification.from_pretrained(
        model_name,
        num_labels=len(CUAD_LABELS),
        id2label=CUAD_LABELS,
        label2id={v: k for k, v in CUAD_LABELS.items()},
        problem_type="multi_label_classification"
    )
classifier = pipeline(
        "text-classification",
        model=model,
        tokenizer=tokenizer,
        return_all_scores=True,
        device=0 if torch.cuda.is_available() else -1
    )
        
# Initialize sentence tokenizer
sentence_tokenizer = PunktSentenceTokenizer()

# Store in global cache
ml_models["classifier"] = classifier
ml_models["tokenizer"] = sentence_tokenizer
ml_models["model_name"] = model_name

async def classify_clauses_batch(
    clauses: List[Dict[str, Any]], 
    threshold: float, 
    batch_size: int
) -> List[RiskyClauseDetails]:

    if "classifier" not in ml_models:
        raise HTTPException(status_code=500, detail="Classification model not loaded")

    classifier = ml_models["classifier"]
    results = []

    for i in range(0, len(clauses), batch_size):
        batch_clauses = clauses[i:i + batch_size]
        clause_texts = [item["clause"] for item in batch_clauses]

        try:
            # Call pipeline synchronously, not inside asyncio.to_thread
            batch_predictions = classifier(clause_texts)
            
            for clause_obj, predictions in zip(batch_clauses, batch_predictions):
                for prediction in predictions:
                    label = prediction["label"]
                    score = prediction["score"]

                    risk_weight = RISK_WEIGHTS.get(label, 1)
                    risk_score = score * risk_weight
                    final_score = risk_score

                    if final_score >= threshold:
                        logger.debug(f"Creating RiskyClauseDetails: clause type={type(clause_obj['clause'])}, label={label}, confidence={final_score}")

                        risky_clause = RiskyClauseDetails(
                            clause = str(clause_obj["clause"]),
                            tag = str(label.replace("_", " ")),
                            confidence = str(f"{final_score:.2f}"),
                            start_index = str(clause_obj["start_index"]),
                            end_index = str(clause_obj["end_index"]),
                            risk_score = str(risk_weight)
                        )
                        results.append(risky_clause)

        except Exception as e:
            logger.error(f"Error processing batch {i//batch_size + 1}: {e}", exc_info=True)
            continue

    return results




def split_clauses_with_offsets(text: str, min_clause_len: int = 30, use_legal_abbreviations: bool = True):
    if "tokenizer" not in ml_models:
        raise HTTPException(status_code=500, detail="Tokenizer not loaded")

    tokenizer = ml_models["tokenizer"]

    # Legal abbreviations that shouldn't trigger sentence boundaries
    legal_abbreviations = {
        'inc.', 'llc.', 'corp.', 'ltd.', 'co.', 'u.s.', 'vs.', 'v.',
        'sec.', 'art.', 'para.', 'cl.', 'no.', 'etc.', 'e.g.', 'i.e.',
        'mr.', 'mrs.', 'dr.', 'prof.', 'jr.', 'sr.'
    }

    # Preprocess text for better legal document handling
    processed_text = text.strip()

    # Get character spans
    spans = list(tokenizer.span_tokenize(processed_text))

    clauses = []
    for start, end in spans:
        if end - start <= min_clause_len:
            continue
            
        clause_text = processed_text[start:end].strip()
        if not clause_text:
            continue
            
        clauses.append({
            "clause": clause_text,
            "start_index": start,
            "end_index": end
        })

    return clauses


async def analyze_risky_clauses(text):
    logger.info("splitting the text into clauses")
    clauses = split_clauses_with_offsets_alternative(text)
        
    if not clauses:
        return []
    
    # Classify clauses
    logger.info("classifying the clauses")
    risky_clauses = await classify_clauses_batch(
        clauses, 
        0.5, 
        16
    )

    return risky_clauses


def preprocess_legal_text(text: str) -> str:

    processed_text = text.strip()

    # Handle enumerated items: (a), (b), (c) -> preserve as single units
    processed_text = re.sub(r'\n\s*\(([a-z])\)\s*', r' (\1) ', processed_text)

    # Handle numbered items: 1., 2., 3. -> preserve as single units  
    processed_text = re.sub(r'\n\s*(\d+)\.\s*', r' \1. ', processed_text)

    # Handle section references: § 123, Section 4.2
    processed_text = re.sub(r'\n\s*(§|Section)\s*(\d+(?:\.\d+)*)\s*', r' \1 \2 ', processed_text)

    # Normalize excessive whitespace while preserving structure
    processed_text = re.sub(r'\s+', ' ', processed_text)

    return processed_text.strip()

def split_clauses_with_offsets_alternative(text: str, min_clause_len: int = 20, use_legal_abbreviations: bool = True):

    if "tokenizer" not in ml_models:
        raise HTTPException(status_code=500, detail="Tokenizer not loaded")

    if use_legal_abbreviations:
        legal_abbreviations_set = {
            'inc', 'llc', 'corp', 'ltd', 'co', 'u.s', 'vs', 'v',
            'sec', 'art', 'para', 'cl', 'no', 'etc', 'e.g', 'i.e',
            'mr', 'mrs', 'dr', 'prof', 'jr', 'sr', 'esq', 'llp',
            'p.c', 'l.p', 'ld', 'hon', 'resp', 'petn', 'appln',
            'fig', 'sub', 'ch', 'pt', 'div', 'app', 'supp',
            'atty', 'ct', 'dist', 'fed', 'gov', 'j', 'jj', 'op'
        }
        
        punkt_params = PunktParameters()
        punkt_params.abbrev_types = legal_abbreviations_set
        
        # Create tokenizer with custom parameters
        tokenizer = PunktSentenceTokenizer(punkt_params)
        
    else:
        tokenizer = ml_models["tokenizer"]

    # Rest of processing
    processed_text = preprocess_legal_text(text)
    spans = list(tokenizer.span_tokenize(processed_text))
    
    clauses = []
    for start, end in spans:
        if end - start <= min_clause_len:
            continue
            
        clause_text = processed_text[start:end].strip()
        if not clause_text:
            continue
            
        clauses.append({
            "clause": clause_text,
            "start_index": start,
            "end_index": end
        })
    
    return clauses
