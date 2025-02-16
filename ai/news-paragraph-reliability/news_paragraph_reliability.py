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
CHROMA_COLLECTION_NAME = "reference_paragraphs"

# ChromaDB 클라이언트 생성 및 컬렉션 가져오기
chroma_client = chromadb.HttpClient(host=CHROMA_HOST, port=CHROMA_PORT)
chroma_collection = chroma_client.get_or_create_collection(name=CHROMA_COLLECTION_NAME)

# 제목과 문단 간 유사도 비교 임계값 설정 (0.6 이상일 때만 분석 수행)
TITLE_SIMILARITY_THRESHOLD = 0.6

# Pydantic 모델 정의
class NewsArticle(BaseModel):
    title: str  # 제목 추가
    paragraphs: List[str]    

class NewsReliabilityRequest(BaseModel):
    news: NewsArticle

# 유틸리티 함수: 제목과 문단 간 코사인 유사도 계산
def compute_similarity(text1: str, text2: str) -> float:
    embedding1 = bert_model.encode([text1])[0]
    embedding2 = bert_model.encode([text2])[0]
    
    # 코사인 유사도 계산
    similarity = np.dot(embedding1, embedding2) / (np.linalg.norm(embedding1) * np.linalg.norm(embedding2))
    return similarity

# 유틸리티 함수: ChromaDB에서 가장 유사한 문단 검색
def query_similar_paragraph(query_text: str, n_results: int = 1):
    query_embedding = bert_model.encode([query_text])[0]
    result = chroma_collection.query(
        query_embeddings=[query_embedding.tolist()],
        n_results=n_results,
        include=["documents", "distances"]
    )
    if result and "documents" in result and result["documents"] and result["documents"][0]:
        best_doc = result["documents"][0][0]
        best_distance = result["distances"][0][0]
        similarity_score = 1 / (1 + best_distance)  # 거리 기반 유사도 계산
        return best_doc, similarity_score
    return None, 0.0

# 유틸리티 함수: ChatGPT로 문단 요약 수행
def summarize_with_chatgpt(text: str) -> str:
    prompt = f"다음 텍스트를 한 문장으로 간결하게 요약해줘: {text}"
    try:
        response = openai.ChatCompletion.create(
            model="gpt-4o",
            messages=[
                {"role": "system", "content": "너는 뉴스 요약 전문가이다."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=120,
            n=1,
            temperature=0.5
        )
        summary = response.choices[0].message["content"].strip()
        return summary
    except Exception as e:
        print(f"ChatGPT 요약 실패: {e}")
        return text

# 문단별 분석 작업 (제목 유사도 검사 -> ChromaDB 검색 -> GPT 요약)
def analyze_paragraph(idx, title, paragraph):
    # 제목과 문단 유사도 검사
    title_similarity = compute_similarity(title, paragraph)
    if title_similarity < TITLE_SIMILARITY_THRESHOLD:
        # 제목과 유사하지 않으면 신뢰도 분석을 건너뜀
        return idx, None, 0.0

    # ChromaDB 문단 검색
    similar_doc, similarity = query_similar_paragraph(paragraph)

    # GPT 요약
    if similar_doc:
        summarized = summarize_with_chatgpt(similar_doc)
    else:
        summarized = None

    return idx, summarized, similarity

@app.post("/news/reliability")
async def analyze_news_reliability(request: NewsReliabilityRequest):
    title = request.news.title  # 제목 가져오기
    news_paragraphs = request.news.paragraphs

    if not news_paragraphs:
        return {
            "paragraph_reliability_scores": [],
            "best_evidence_paragraphs": []
        }

    # 초기화
    best_evidence_paragraphs = [None] * len(news_paragraphs)
    reliability_scores = [0.0] * len(news_paragraphs)

    # 병렬 실행 (ThreadPoolExecutor 사용)
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