#!/usr/bin/env python
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
import chromadb
from sentence_transformers import SentenceTransformer
import numpy as np
import torch
import os
from concurrent.futures import ThreadPoolExecutor

app = FastAPI()

device = "cuda" if torch.cuda.is_available() else "cpu"

# ✅ 더 강력한 모델 사용 (MPNet, 768차원)
bert_model = SentenceTransformer("all-mpnet-base-v2", device=device)

# ChromaDB 연결 정보
CHROMA_HOST = "i12d208.p.ssafy.io"
CHROMA_PORT = 8000
REFERENCE_PARAGRAPH_COLLECTION_NAME = "reference_paragraphs_v2"

chroma_client = chromadb.HttpClient(host=CHROMA_HOST, port=CHROMA_PORT)

# ✅ 차원을 명시하여 768차원 컬렉션 사용
reference_paragraph_collection = chroma_client.get_or_create_collection(
    name=REFERENCE_PARAGRAPH_COLLECTION_NAME,
    dimension=768
)

# 유사도 비교 임계값
SIMILARITY_THRESHOLD = 0.6


class NewsArticle(BaseModel):
    title: str
    paragraphs: List[str]


class NewsReliabilityRequest(BaseModel):
    news: NewsArticle
    referenceParagraphIds: List[str]


def fetch_reference_embeddings(reference_ids):
    result = reference_paragraph_collection.get(ids=reference_ids, include=["documents", "embeddings"])
    id_to_embedding = {}
    id_to_text = {}
    for i, doc_id in enumerate(result["ids"]):
        id_to_embedding[doc_id] = np.array(result["embeddings"][i])
        id_to_text[doc_id] = result["documents"][i]
    return id_to_embedding, id_to_text


def compute_similarity(embedding1, embedding2):
    return np.dot(embedding1, embedding2) / (np.linalg.norm(embedding1) * np.linalg.norm(embedding2))


def analyze_paragraph(idx, paragraph_embedding, reference_embeddings, reference_texts):
    best_similarity = -1
    best_reference_id = None

    for ref_id, ref_embedding in reference_embeddings.items():
        similarity = compute_similarity(paragraph_embedding, ref_embedding)
        if similarity > best_similarity:
            best_similarity = similarity
            best_reference_id = ref_id

    if best_similarity < SIMILARITY_THRESHOLD:
        best_reference_text = "신뢰할 수 있는 참고 문단을 찾지 못했습니다."
    else:
        best_reference_text = reference_texts.get(best_reference_id, "매칭 실패")

    return idx, best_reference_text, best_similarity


@app.post("/news/reliability")
async def analyze_news_reliability(request: NewsReliabilityRequest):
    title = request.news.title
    news_paragraphs = request.news.paragraphs
    reference_ids = request.referenceParagraphIds

    if not news_paragraphs or not reference_ids:
        return {
            "paragraph_reliability_scores": [],
            "best_evidence_paragraphs": []
        }

    reference_embeddings, reference_texts = fetch_reference_embeddings(reference_ids)

    paragraph_embeddings = bert_model.encode(news_paragraphs)

    best_evidence_paragraphs = [None] * len(news_paragraphs)
    reliability_scores = [0.0] * len(news_paragraphs)

    with ThreadPoolExecutor(max_workers=5) as executor:
        futures = [
            executor.submit(analyze_paragraph, i, paragraph_embeddings[i], reference_embeddings, reference_texts)
            for i in range(len(news_paragraphs))
        ]

        for future in futures:
            idx, best_text, similarity = future.result()
            best_evidence_paragraphs[idx] = best_text
            reliability_scores[idx] = similarity

    return {
        "paragraph_reliability_scores": reliability_scores,
        "best_evidence_paragraphs": best_evidence_paragraphs
    }


@app.post("/get-similar-references")
async def get_similar_references(request: dict):
    query = request.get("query")
    n_results = int(request.get("n_results", 5))

    if not query:
        raise HTTPException(status_code=400, detail="Query text is required")

    query_embedding = bert_model.encode([query])[0]

    result = reference_paragraph_collection.query(
        query_embeddings=[query_embedding.tolist()],
        n_results=n_results,
        include=["documents", "distances", "ids"]
    )

    if not result or "ids" not in result or not result["ids"]:
        return {"reference_ids": []}

    reference_ids = result["ids"][0]
    return {"reference_ids": reference_ids}
