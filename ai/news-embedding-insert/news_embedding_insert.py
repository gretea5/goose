from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
from sentence_transformers import SentenceTransformer
import chromadb
from chromadb.utils import embedding_functions

app = FastAPI()

# ChromaDB 설정
CHROMA_HOST = "i12d208.p.ssafy.io"
CHROMA_PORT = 8000

# 뉴스 기사 컬렉션 이름
TITLE_COLLECTION_NAME = "news_articles_title"
CONTENT_COLLECTION_NAME = "news_articles_content"
PARAGRAPH_COLLECTION_NAME = "news_articles_paragraphs"

# 레퍼런스 뉴스 컬렉션 이름
REFERENCE_PARAGRAPH_COLLECTION_NAME = "reference_paragraphs"
REFERENCE_CONTENT_COLLECTION_NAME = "reference_content"

embedding_model = SentenceTransformer("all-MiniLM-L6-v2")
embedding_function = embedding_functions.SentenceTransformerEmbeddingFunction(model_name="all-MiniLM-L6-v2")

chroma_client = chromadb.HttpClient(host=CHROMA_HOST, port=CHROMA_PORT)

# 뉴스 기사 컬렉션
title_collection = chroma_client.get_or_create_collection(name=TITLE_COLLECTION_NAME)
content_collection = chroma_client.get_or_create_collection(name=CONTENT_COLLECTION_NAME)
paragraph_collection = chroma_client.get_or_create_collection(name=PARAGRAPH_COLLECTION_NAME)

# 레퍼런스 뉴스 컬렉션
reference_paragraph_collection = chroma_client.get_or_create_collection(name=REFERENCE_PARAGRAPH_COLLECTION_NAME)
reference_content_collection = chroma_client.get_or_create_collection(name=REFERENCE_CONTENT_COLLECTION_NAME)


class NewsArticle(BaseModel):
    id: str
    title: str
    content: str
    paragraphs: List[str]
    pubDate: Optional[str] = None


class ReferenceNews(BaseModel):
    id: str
    title: str
    content: str
    paragraphs: List[str]
    pubDate: Optional[str] = None


@app.post("/store-news")
async def store_news(news: NewsArticle):
    try:
        # 제목 임베딩 및 저장
        title_embedding = embedding_model.encode(news.title).tolist()
        title_collection.upsert(
            ids=[f"{news.id}_title"],
            documents=[news.title],
            embeddings=[title_embedding],
            metadatas=[{"news_id": news.id, "pubDate": news.pubDate, "type": "title"}]
        )

        # 본문 임베딩 및 저장
        content_embedding = embedding_model.encode(news.content).tolist()
        content_collection.upsert(
            ids=[f"{news.id}_content"],
            documents=[news.content],
            embeddings=[content_embedding],
            metadatas=[{"news_id": news.id, "pubDate": news.pubDate, "type": "content"}]
        )

        # 문단별 임베딩 및 저장
        for idx, paragraph in enumerate(news.paragraphs):
            paragraph_embedding = embedding_model.encode(paragraph).tolist()
            paragraph_collection.upsert(
                ids=[f"{news.id}_p_{idx}"],
                documents=[paragraph],
                embeddings=[paragraph_embedding],
                metadatas=[{"news_id": news.id, "pubDate": news.pubDate, "type": "paragraph"}]
            )

        # 확인 로그 추가
        print(f"▶ 제목 임베딩 저장: {news.id}_title - {title_embedding}")
        print(f"▶ 본문 임베딩 저장: {news.id}_content - {content_embedding}")

        return {"status": "success", "message": f"News {news.id} stored successfully"}

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to store news: {str(e)}")


@app.post("/store-reference-news")
async def store_reference_news(reference_news: ReferenceNews):
    try:
        # 문단별 임베딩 및 저장 (reference_paragraphs)
        for idx, paragraph in enumerate(reference_news.paragraphs):
            paragraph_embedding = embedding_model.encode(paragraph).tolist()
            reference_paragraph_collection.upsert(
                ids=[f"{reference_news.id}_p_{idx}"],
                documents=[paragraph],
                embeddings=[paragraph_embedding],
                metadatas=[{"news_id": reference_news.id, "pubDate": reference_news.pubDate, "type": "paragraph"}]
            )

        # 전체 본문 임베딩 및 저장 (reference_content)
        content_embedding = embedding_model.encode(reference_news.content).tolist()
        reference_content_collection.upsert(
            ids=[f"{reference_news.id}_content"],
            documents=[reference_news.content],
            embeddings=[content_embedding],
            metadatas=[{"news_id": reference_news.id, "pubDate": reference_news.pubDate, "type": "content"}]
        )

        return {"status": "success", "message": f"Reference News {reference_news.id} stored successfully"}

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to store reference news: {str(e)}")
