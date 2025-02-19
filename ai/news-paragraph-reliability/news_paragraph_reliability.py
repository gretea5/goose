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

# ✅ 더 강력한 목록 사용 (MPNet, 768차원)
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
SIMILARITY_THRESHOLD = 0.8


class NewsArticle(BaseModel):
    title: str
    paragraphs: List[str]


class NewsReliabilityRequest(BaseModel):
    news: NewsArticle
    referenceParagraphIds: List[str]


# 참고 문단 이본디버거를 획득
def fetch_reference_embeddings(reference_ids):
    result = reference_paragraph_collection.get(
        ids=reference_ids, include=["documents", "embeddings"]
    )
    id_to_embedding = {}
    id_to_text = {}
    for i, doc_id in enumerate(result["ids"]):
        id_to_embedding[doc_id] = np.array(result["embeddings"][i])
        id_to_text[doc_id] = result["documents"][i]
    return id_to_embedding, id_to_text


# 코사인 유사도 계습
def compute_similarity(embedding1, embedding2):
    return np.dot(embedding1, embedding2) / (
        np.linalg.norm(embedding1) * np.linalg.norm(embedding2)
    )


# 문단 보고 유사도 계산
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
        best_similarity = 0.0
    else:
        best_reference_text = reference_texts.get(best_reference_id, "매칭 실패")

    return idx, best_reference_text, best_similarity


# 문단 필터링 함수 추가
# 글자수 높은 공고 또는 기자 이름, 예술, 제보 통보들은 제외
# 복지 하려면 요청해!
def is_valid_paragraph(paragraph):
    if len(paragraph.strip()) <= 10:
        return False
    lower_p = paragraph.lower()
    filtering_patterns = [
        "기자", "촬영", "영상편집", "디자인", "제보", "카카오톡", "ytn",
        "social@", "메일", "전화", "보도", "연락처", "광고", "구독"
    ]
    return not any(pattern in lower_p for pattern in filtering_patterns)


@app.post("/news/reliability")
async def analyze_news_reliability(request: NewsReliabilityRequest):
    title = request.news.title
    news_paragraphs = request.news.paragraphs
    reference_ids = request.referenceParagraphIds

    if not news_paragraphs or not reference_ids:
        return {"paragraph_reliability_scores": [], "best_evidence_paragraphs": []}

    reference_embeddings, reference_texts = fetch_reference_embeddings(reference_ids)

    # 문단 임베딩 계산
    paragraph_embeddings = embedding_model.encode(news_paragraphs, show_progress_bar=False)

    best_evidence_paragraphs = []
    reliability_scores = []

    for i, paragraph_embedding in enumerate(paragraph_embeddings):
        # 문단 내용이 부적절하면 필터링 메시지 반환
        if not is_valid_paragraph(news_paragraphs[i]):
            best_evidence_paragraphs.append("신뢰할 수 있는 참고 문단을 찾지 못했습니다.")
            reliability_scores.append(0.0)
        else:
            _, best_text, similarity = analyze_paragraph(
                i, paragraph_embedding, reference_embeddings, reference_texts
            )
            best_evidence_paragraphs.append(best_text)
            reliability_scores.append(similarity)

    return {"paragraph_reliability_scores": reliability_scores, "best_evidence_paragraphs": best_evidence_paragraphs}



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
