#!/usr/bin/env python
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
import chromadb
from sentence_transformers import SentenceTransformer
import numpy as np
import torch
import os

app = FastAPI()

# ✅ GPU 사용 가능 여부 설정
device = "cuda" if torch.cuda.is_available() else "cpu"

# ✅ 더 강력한 모델 사용 (MPNet, 768차원)
embedding_model = SentenceTransformer("all-mpnet-base-v2", device=device)

# ✅ ChromaDB 연결 정보 (0.4.24 서버 사용)
CHROMA_HOST = "i12d208.p.ssafy.io"
CHROMA_PORT = 8001
REFERENCE_PARAGRAPH_COLLECTION_NAME = "reference_paragraphs_v2"

chroma_client = chromadb.HttpClient(host=CHROMA_HOST, port=CHROMA_PORT)

reference_paragraph_collection = chroma_client.get_or_create_collection(
    name=REFERENCE_PARAGRAPH_COLLECTION_NAME
)

# ✅ 유사도 비교 임계값 설정
SIMILARITY_THRESHOLD = 0.6


class NewsArticle(BaseModel):
    title: str
    paragraphs: List[str]


class NewsReliabilityRequest(BaseModel):
    news: NewsArticle
    referenceParagraphIds: List[str]


def fetch_reference_embeddings(reference_ids):
    """레퍼런스 문단 ID 목록을 통해 임베딩 및 문단 텍스트 가져오기"""
    result = reference_paragraph_collection.get(
        ids=reference_ids, include=["documents", "embeddings"]
    )

    id_to_embedding = {}
    id_to_text = {}

    for i, doc_id in enumerate(result["ids"]):
        id_to_embedding[doc_id] = np.array(result["embeddings"][i])
        id_to_text[doc_id] = result["documents"][i]

    return id_to_embedding, id_to_text


def compute_similarity(embedding1, embedding2):
    """코사인 유사도 계산"""
    return np.dot(embedding1, embedding2) / (
        np.linalg.norm(embedding1) * np.linalg.norm(embedding2)
    )


def analyze_paragraph(idx, paragraph_embedding, reference_embeddings, reference_texts):
    """문단별 유사도 분석"""
    best_similarity = -1
    best_reference_id = None

    for ref_id, ref_embedding in reference_embeddings.items():
        similarity = compute_similarity(paragraph_embedding, ref_embedding)
        if similarity > best_similarity:
            best_similarity = similarity
            best_reference_id = ref_id

    # ✅ 유사도가 임계값보다 낮으면, 신뢰할 수 있는 문단이 없다고 처리
    if best_similarity < SIMILARITY_THRESHOLD:
        best_reference_text = "신뢰할 수 있는 참고 문단을 찾지 못했습니다."
        best_similarity = 0.0  # ✅ 안전하게 0.0으로 설정
    else:
        best_reference_text = reference_texts.get(best_reference_id, "매칭 실패")

    return idx, best_reference_text, best_similarity


@app.post("/news/reliability")
async def analyze_news_reliability(request: NewsReliabilityRequest):
    """뉴스 문단 신뢰도 분석 API"""
    title = request.news.title
    news_paragraphs = request.news.paragraphs
    reference_ids = request.referenceParagraphIds

    if not news_paragraphs or not reference_ids:
        return {
            "paragraph_reliability_scores": [],
            "best_evidence_paragraphs": [],
        }

    # 1️⃣ 레퍼런스 임베딩 가져오기
    reference_embeddings, reference_texts = fetch_reference_embeddings(reference_ids)

    # 2️⃣ 문단 임베딩 계산 (MPNet 모델 활용)
    paragraph_embeddings = embedding_model.encode(
        news_paragraphs, show_progress_bar=False
    )

    # 3️⃣ 순차 처리로 신뢰도 및 근거 문단 계산
    best_evidence_paragraphs = []
    reliability_scores = []

    for i, paragraph_embedding in enumerate(paragraph_embeddings):
        _, best_text, similarity = analyze_paragraph(
            i, paragraph_embedding, reference_embeddings, reference_texts
        )
        best_evidence_paragraphs.append(best_text)
        reliability_scores.append(similarity)

    return {
        "paragraph_reliability_scores": reliability_scores,
        "best_evidence_paragraphs": best_evidence_paragraphs,
    }


@app.post("/get-similar-references")
async def get_similar_references(request: dict):
    """참고 문단 유사 검색 API"""
    query = request.get("query")
    n_results = int(request.get("n_results", 5))

    if not query:
        raise HTTPException(status_code=400, detail="Query text is required")

    # 1️⃣ 쿼리 문장 임베딩 계산
    query_embedding = embedding_model.encode([query])[0]

    # 2️⃣ ChromaDB에서 유사 문단 검색
    result = reference_paragraph_collection.query(
        query_embeddings=[query_embedding.tolist()],
        n_results=n_results,
        include=["documents", "distances"],
    )

    # 3️⃣ 결과 없을 경우 빈 리스트 반환
    if not result or not result.get("ids"):
        return {"reference_ids": [], "reference_documents": [], "distances": []}

    reference_ids = result.get("ids", [[]])[0]
    reference_documents = result.get("documents", [[]])[0]
    distances = result.get("distances", [[]])[0]

    return {
        "reference_ids": reference_ids,
        "reference_documents": reference_documents,
        "distances": distances,
    }


@app.post("/get-similar-news-articles")
async def get_similar_news_articles(request: dict):
    """일반 뉴스 기사 유사 검색 API"""
    query = request.get("query")
    n_results = int(request.get("n_results", 5))

    if not query:
        raise HTTPException(status_code=400, detail="Query text is required")

    # 1️⃣ 쿼리 문장 임베딩 계산
    query_embedding = embedding_model.encode([query])[0]

    # 2️⃣ 일반 뉴스 기사 내용(news_articles_content_v2) 컬렉션에서 유사 기사 검색
    result = chroma_client.get_or_create_collection(name="news_articles_content_v2").query(
    query_embeddings=[query_embedding.tolist()],
    n_results=n_results,
    include=["documents", "metadatas", "distances"],
)


    # 3️⃣ 결과 없을 경우 빈 리스트 반환
    if not result or not result.get("ids"):
        return {"news_article_ids": [], "documents": [], "distances": []}

    news_article_ids = result.get("ids", [[]])[0]
    documents = result.get("documents", [[]])[0]
    distances = result.get("distances", [[]])[0]

    return {
        "news_article_ids": news_article_ids,
        "documents": documents,
        "distances": distances,
    }
