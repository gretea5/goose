#!/usr/bin/env python
from fastapi import FastAPI
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

# 제목과 문단 간 유사도 비교 임계값 설정 (0.6 이상일 때만 분석 수행)
TITLE_SIMILARITY_THRESHOLD = 0.6

# Pydantic 모델 정의
class NewsArticle(BaseModel):
    title: str  # 제목 추가
    paragraphs: List[str]    

class NewsReliabilityRequest(BaseModel):
    news: NewsArticle

def query_similar_paragraph(query_text: str, n_results: int = 1):
    result = reference_paragraph_collection.query(
        query_texts=[query_text],  # 임베딩 대신 raw text로 검색!
        n_results=n_results,
        include=["documents", "distances"]
    )
    if result and result["documents"] and result["documents"][0]:
        best_doc = result["documents"][0][0]
        best_distance = result["distances"][0][0]
        similarity_score = 1 / (1 + best_distance)  # 거리 → 유사도 변환
        return best_doc, similarity_score
    return None, 0.0


def analyze_paragraph(idx, title, paragraph):
    similar_doc, similarity = query_similar_paragraph(paragraph)

    if not similar_doc:
        return idx, None, 0.0

    return idx, similar_doc, similarity


@app.post("/news/reliability")
async def analyze_news_reliability(request: NewsReliabilityRequest):
    title = request.news.title
    news_paragraphs = request.news.paragraphs

    if not news_paragraphs:
        return {
            "paragraph_reliability_scores": [],
            "best_evidence_paragraphs": []
        }

    best_evidence_paragraphs = [None] * len(news_paragraphs)
    reliability_scores = [0.0] * len(news_paragraphs)

    with ThreadPoolExecutor(max_workers=5) as executor:
        futures = [executor.submit(analyze_paragraph, i, title, para) for i, para in enumerate(news_paragraphs)]

        for future in futures:
            idx, summarized, similarity = future.result()
            best_evidence_paragraphs[idx] = summarized
            reliability_scores[idx] = similarity

    return {
        "paragraph_reliability_scores": reliability_scores,
        "best_evidence_paragraphs": best_evidence_paragraphs
    }