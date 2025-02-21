#!/usr/bin/env python
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import chromadb
from sentence_transformers import SentenceTransformer
import numpy as np
import torch
import os
import openai

# OpenAI API Key 설정 (실제 키로 교체)
openai.api_key = os.environ.get("OPENAI_API_KEY", "")

app = FastAPI()

device = "cuda" if torch.cuda.is_available() else "cpu"


# SentenceTransformer 모델 초기화 (임베딩 계산용)
embedding_model = SentenceTransformer("all-MiniLM-L6-v2")

# ChromaDB 연결 정보
CHROMA_HOST = "i12d208.p.ssafy.io"
CHROMA_PORT = 8000

# ChromaDB 클라이언트 생성
chroma_client = chromadb.HttpClient(host=CHROMA_HOST, port=CHROMA_PORT)

# 컬렉션 초기화
reference_news_chroma_collection_paragraphs = chroma_client.get_or_create_collection(name="reference_paragraphs")
reference_news_chroma_collection_content = chroma_client.get_or_create_collection(name="reference_content")
news_articles_chroma_collection_title = chroma_client.get_or_create_collection(name="news_articles_title")
news_articles_chroma_collection_content = chroma_client.get_or_create_collection(name="news_articles_content")
news_articles_chroma_collection_paragraphs = chroma_client.get_or_create_collection(name="news_articles_paragraphs")

# Pydantic 모델 정의
class TitleAnalysisRequest(BaseModel):
    newsId: str
    referenceNewsId: str

class ParagraphAnalysisRequest(BaseModel):
    newsId: str
    referenceNewsId: str
    paragraphIndices: Optional[List[int]] = None

class NewsAnalysisRequest(BaseModel):
    newsId: str
    referenceNewsId: str


# 유틸리티 함수: 코사인 유사도 계산
def compute_cosine_similarity(vec1: np.ndarray, vec2: np.ndarray) -> float:
    return float(np.dot(vec1, vec2) / (np.linalg.norm(vec1) * np.linalg.norm(vec2)))


# 🔹 제목과 참조 뉴스 내용 비교
@app.post("/title-compare-contents")
async def title_bias_analyse(request: TitleAnalysisRequest):
    try:
        news_id = f"{request.newsId}_title"
        ref_id = f"{request.referenceNewsId}_content"

        print(f"🔎 조회하려는 뉴스 ID: {news_id}")
        print(f"🔎 조회하려는 참조 뉴스 ID: {ref_id}")

        title_result = news_articles_chroma_collection_title.get(
            ids=[news_id], include=["embeddings", "documents", "metadatas"]
        )
        print(f"🔎 제목 데이터 결과: {title_result}")

        if not title_result["documents"]:
            raise HTTPException(status_code=404, detail="뉴스 제목을 찾을 수 없음")

        ref_result = reference_news_chroma_collection_content.get(
            ids=[ref_id], include=["embeddings", "documents", "metadatas"]
        )
        print(f"🔎 참조 뉴스 데이터 결과: {ref_result}")

        if not ref_result["documents"]:
            raise HTTPException(status_code=404, detail="레퍼런스 뉴스 내용을 찾을 수 없음")

        news_title = title_result["documents"][0]
        title_embedding = np.array(title_result["embeddings"][0])

        ref_content = ref_result["documents"][0]
        ref_embedding = np.array(ref_result["embeddings"][0])

        similarity = compute_cosine_similarity(title_embedding, ref_embedding)

        return {
            "title": news_title,
            "reference_content": ref_content,
            "similarity_score": similarity
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))



# 🔹 문단 간 유사도 비교
@app.post("/paragraph-compare-contents")
async def paragraph_bias_analyse(request: ParagraphAnalysisRequest):
    try:
        if request.paragraphIndices:
            query_ids = [f"{request.newsId}_p_{idx}" for idx in request.paragraphIndices]
        else:
            raise HTTPException(status_code=400, detail="문단 인덱스를 제공해주세요.")

        news_paragraphs = news_articles_chroma_collection_paragraphs.get(
            ids=query_ids, include=["embeddings", "documents", "metadatas"]
        )

        if not news_paragraphs["documents"]:
            raise HTTPException(status_code=404, detail="뉴스 기사 문단을 찾을 수 없음")

        ref_paragraphs = reference_news_chroma_collection_paragraphs.get(
            ids=[f"{request.referenceNewsId}_p_{idx}" for idx in range(10)],
            include=["embeddings", "documents", "metadatas"]
        )

        if not ref_paragraphs["documents"]:
            raise HTTPException(status_code=404, detail="레퍼런스 뉴스 문단을 찾을 수 없음")

        results = []
        for i, news_paragraph in enumerate(news_paragraphs["documents"]):
            news_embedding = np.array(news_paragraphs["embeddings"][i])

            similarities = [
                compute_cosine_similarity(news_embedding, np.array(ref_embedding))
                for ref_embedding in ref_paragraphs["embeddings"]
            ]

            max_similarity = max(similarities) if similarities else 0.0
            results.append({
                "paragraph_index": request.paragraphIndices[i],
                "similarity": max_similarity
            })

        return {"similarities": results}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
