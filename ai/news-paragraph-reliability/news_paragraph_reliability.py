#!/usr/bin/env python
from fastapi import FastAPI
from fastapi import HTTPException
from pydantic import BaseModel
from typing import List
import openai
import chromadb
from sentence_transformers import SentenceTransformer
import numpy as np
import torch
import os
from concurrent.futures import ThreadPoolExecutor

# OpenAI API Key 설정 (실제 키로 교체)
openai.api_key = os.environ.get("OPENAI_API_KEY", "")

app = FastAPI()

device = "cuda" if torch.cuda.is_available() else "cpu"

# SentenceTransformer 모델 로드 (임베딩 계산용)
bert_model = SentenceTransformer("all-MiniLM-L6-v2", device=device)

# ChromaDB 연결 정보 (뉴스 reference 문단 임베딩이 저장된 컬렉션)
CHROMA_HOST = "i12d208.p.ssafy.io"
CHROMA_PORT = 8000
REFERENCE_PARAGRAPH_COLLECTION_NAME = "reference_paragraphs"

# ChromaDB 클라이언트 생성 및 컬렉션 가져오기
chroma_client = chromadb.HttpClient(host=CHROMA_HOST, port=CHROMA_PORT)
reference_paragraph_collection = chroma_client.get_or_create_collection(name=REFERENCE_PARAGRAPH_COLLECTION_NAME)

# 제목과 문단 간 유사도 비교 임계값 설정 (0.8 이상일 때만 분석 수행)
TITLE_SIMILARITY_THRESHOLD = 0.8

# Pydantic 모델 정의
class NewsArticle(BaseModel):
    title: str  # 제목 추가
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
    similarity = np.dot(embedding1, embedding2) / (np.linalg.norm(embedding1) * np.linalg.norm(embedding2))
    return similarity


def analyze_paragraph(idx, paragraph_embedding, reference_embeddings, reference_texts):
    best_similarity = -1
    best_reference_id = None

    for ref_id, ref_embedding in reference_embeddings.items():
        similarity = compute_similarity(paragraph_embedding, ref_embedding)
        if similarity > best_similarity:
            best_similarity = similarity
            best_reference_id = ref_id

    best_reference_text = reference_texts.get(best_reference_id, "매칭 실패")

    # ✅ 신뢰도 조건 적용
    if best_similarity < 0.8:
        best_reference_text = "신뢰할 수 있는 참고 문단을 찾지 못했습니다."
    elif best_similarity >= 0.98:
        best_reference_text = "5건의 타 뉴스기사와 비교한 결과 올바른 내용입니다."

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

    # 1. 레퍼런스 임베딩 한번에 가져오기
    reference_embeddings, reference_texts = fetch_reference_embeddings(reference_ids)

    # 2. 문단 임베딩 한번에 계산하기
    paragraph_embeddings = bert_model.encode(news_paragraphs)

    # 3. 병렬 실행
    best_evidence_paragraphs = [None] * len(news_paragraphs)
    reliability_scores = [0.0] * len(news_paragraphs)

    with ThreadPoolExecutor(max_workers=5) as executor:
        futures = [executor.submit(analyze_paragraph, i, paragraph_embeddings[i], reference_embeddings, reference_texts)
                   for i in range(len(news_paragraphs))]

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
        include=["documents", "distances"]  # "ids" 제거!
    )


    if not result or "ids" not in result or not result["ids"]:
        return {"reference_ids": []}

    reference_ids = result["ids"][0]
    return {"reference_ids": reference_ids}
