#!/usr/bin/env python
import os
import time
import schedule
from pymongo import MongoClient
from sentence_transformers import SentenceTransformer
import chromadb
from chromadb.config import Settings
import numpy as np

# 환경변수 또는 기본값으로 MongoDB 연결 정보 설정
MONGO_URI = os.environ.get("MONGO_URI", "mongodb://d208:rnal2qksghkdlxld!@i12d208.p.ssafy.io:27017/goose?authSource=goose")
DATABASE_NAME = "goose"
# MongoDB 컬렉션 이름들
REFERENCE_NEWS_COLLECTION_NAME = "reference_news"
NEWS_ARTICLES_COLLECTION_NAME = "news_articles"

# ChromaDB 연결 정보 (내부 포트 8000에서 실행 중)
CHROMA_HOST = "i12d208.p.ssafy.io"
CHROMA_PORT = 8000

# ChromaDB 컬렉션 이름들
REFERENCE_NEWS_CHROMA_COLLECTION_PARAGRAPHS = "reference_paragraphs"
REFERENCE_NEWS_CHROMA_COLLECTION_CONTENT = "reference_content"
NEWS_ARTICLES_CHROMA_COLLECTION_TITLE = "news_articles_title"
NEWS_ARTICLES_CHROMA_COLLECTION_CONTENT = "news_articles_content"
NEWS_ARTICLES_CHROMA_COLLECTION_PARAGRAPHS = "news_articles_paragraphs"

# 임베딩 계산을 위한 SentenceTransformer 모델 로드
embedding_model = SentenceTransformer("all-MiniLM-L6-v2")

# MongoDB 연결
mongo_client = MongoClient(MONGO_URI)
db = mongo_client[DATABASE_NAME]
reference_news_collection = db[REFERENCE_NEWS_COLLECTION_NAME]
news_articles_collection = db[NEWS_ARTICLES_COLLECTION_NAME]

# ChromaDB 클라이언트 생성 (HTTP 클라이언트를 사용)
chroma_client = chromadb.HttpClient(host=CHROMA_HOST, port=CHROMA_PORT)
# 참조 뉴스용 컬렉션 생성
reference_news_chroma_collection_paragraphs = chroma_client.get_or_create_collection(name=REFERENCE_NEWS_CHROMA_COLLECTION_PARAGRAPHS)
reference_news_chroma_collection_content = chroma_client.get_or_create_collection(name=REFERENCE_NEWS_CHROMA_COLLECTION_CONTENT)
# 뉴스 기사용 컬렉션 생성
news_articles_chroma_collection_title = chroma_client.get_or_create_collection(name=NEWS_ARTICLES_CHROMA_COLLECTION_TITLE)
news_articles_chroma_collection_content = chroma_client.get_or_create_collection(name=NEWS_ARTICLES_CHROMA_COLLECTION_CONTENT)
news_articles_chroma_collection_paragraphs = chroma_client.get_or_create_collection(name=NEWS_ARTICLES_CHROMA_COLLECTION_PARAGRAPHS)

def update_reference_news_embeddings():
    print("=== 참조 뉴스 ChromaDB 임베딩 업데이트 시작 ===")
    documents = list(reference_news_collection.find({}))
    for doc in documents:
        news_id = str(doc.get("_id"))
        title = doc.get("title", "")
        pubDate = doc.get("pubDate", "")
        
        # 1. 문단 임베딩 저장 (참조 뉴스 문단)
        paragraphs = doc.get("paragraphs", [])
        for idx, paragraph in enumerate(paragraphs):
            embedding = embedding_model.encode(paragraph).tolist()
            doc_id = f"{news_id}_p_{idx}"
            metadata = {
                "news_id": news_id,
                "title": title,
                "pubDate": pubDate,
                "type": "paragraph"
            }
            try:
                reference_news_chroma_collection_paragraphs.add(
                    ids=[doc_id],
                    documents=[paragraph],
                    embeddings=[embedding],
                    metadatas=[metadata]
                )
            except Exception as e:
                print(f"참조 뉴스 문단 저장 실패 (doc_id: {doc_id}): {e}")
        
        # 2. 전체 기사 내용 임베딩 저장 (참조 뉴스 전체 내용)
        content_text = doc.get("content", "")
        if content_text:
            embedding = embedding_model.encode(content_text).tolist()
            doc_id = f"{news_id}_full"
            metadata = {
                "news_id": news_id,
                "title": title,
                "pubDate": pubDate,
                "type": "full_content"
            }
            try:
                reference_news_chroma_collection_content.add(
                    ids=[doc_id],
                    documents=[content_text],
                    embeddings=[embedding],
                    metadatas=[metadata]
                )
            except Exception as e:
                print(f"참조 뉴스 전체 내용 저장 실패 (doc_id: {doc_id}): {e}")
    print("=== 참조 뉴스 ChromaDB 임베딩 업데이트 완료 ===")

def update_news_articles_embeddings():
    print("=== 뉴스 기사 ChromaDB 임베딩 업데이트 시작 ===")
    documents = list(news_articles_collection.find({}))
    for doc in documents:
        news_id = str(doc.get("_id"))
        title = doc.get("title", "")
        pubDate = doc.get("pubDate", "")
        
        # 1. 제목 임베딩 저장
        if title:
            embedding = embedding_model.encode(title).tolist()
            doc_id = f"{news_id}_title"
            metadata = {
                "news_id": news_id,
                "title": title,
                "pubDate": pubDate,
                "type": "title"
            }
            try:
                news_articles_chroma_collection_title.add(
                    ids=[doc_id],
                    documents=[title],
                    embeddings=[embedding],
                    metadatas=[metadata]
                )
            except Exception as e:
                print(f"뉴스 기사 제목 저장 실패 (doc_id: {doc_id}): {e}")
        
        # 2. 전체 기사 내용 임베딩 저장
        content_text = doc.get("content", "")
        if content_text:
            embedding = embedding_model.encode(content_text).tolist()
            doc_id = f"{news_id}_content"
            metadata = {
                "news_id": news_id,
                "title": title,
                "pubDate": pubDate,
                "type": "content"
            }
            try:
                news_articles_chroma_collection_content.add(
                    ids=[doc_id],
                    documents=[content_text],
                    embeddings=[embedding],
                    metadatas=[metadata]
                )
            except Exception as e:
                print(f"뉴스 기사 내용 저장 실패 (doc_id: {doc_id}): {e}")
        
        # 3. 문단 임베딩 저장
        paragraphs = doc.get("paragraphs", [])
        for idx, paragraph in enumerate(paragraphs):
            embedding = embedding_model.encode(paragraph).tolist()
            doc_id = f"{news_id}_p_{idx}"
            metadata = {
                "news_id": news_id,
                "title": title,
                "pubDate": pubDate,
                "type": "paragraph"
            }
            try:
                news_articles_chroma_collection_paragraphs.add(
                    ids=[doc_id],
                    documents=[paragraph],
                    embeddings=[embedding],
                    metadatas=[metadata]
                )
            except Exception as e:
                print(f"뉴스 기사 문단 저장 실패 (doc_id: {doc_id}): {e}")
    print("=== 뉴스 기사 ChromaDB 임베딩 업데이트 완료 ===")

def update_chroma_embeddings():
    print("=== ChromaDB 전체 임베딩 업데이트 시작 ===")
    update_reference_news_embeddings()
    update_news_articles_embeddings()
    print("=== ChromaDB 전체 임베딩 업데이트 완료 ===")

def job():
    update_chroma_embeddings()

if __name__ == "__main__":
    print("임베딩 업데이트 스케줄러 시작...")
    job()  # 실행 즉시 한 번 작업 수행
    # 이후 24시간마다 실행 (필요에 따라 조정 가능)
    schedule.every(24).hours.do(job)
    while True:
        schedule.run_pending()
        time.sleep(60)  # 1분마다 스케줄 체크
