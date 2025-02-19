#!/usr/bin/env python
import os
import time
import schedule
from pymongo import MongoClient
from sentence_transformers import SentenceTransformer
import chromadb
import numpy as np

MONGO_URI = os.environ.get("MONGO_URI")
DATABASE_NAME = "goose"
REFERENCE_NEWS_COLLECTION_NAME = "reference_news"
NEWS_ARTICLES_COLLECTION_NAME = "news_articles"

CHROMA_HOST = "i12d208.p.ssafy.io"
CHROMA_PORT = 8000

# 새로 생성할 768차원 ChromaDB 컬렉션명 (v2 명칭 추가)
REFERENCE_PARAGRAPH_COLLECTION_V2 = "reference_paragraphs_v2"
REFERENCE_CONTENT_COLLECTION_V2 = "reference_content_v2"
NEWS_TITLE_COLLECTION_V2 = "news_articles_title_v2"
NEWS_CONTENT_COLLECTION_V2 = "news_articles_content_v2"
NEWS_PARAGRAPH_COLLECTION_V2 = "news_articles_paragraphs_v2"

embedding_model = SentenceTransformer("all-mpnet-base-v2")

mongo_client = MongoClient(MONGO_URI)
db = mongo_client[DATABASE_NAME]
reference_news_collection = db[REFERENCE_NEWS_COLLECTION_NAME]
news_articles_collection = db[NEWS_ARTICLES_COLLECTION_NAME]

chroma_client = chromadb.HttpClient(host=CHROMA_HOST, port=CHROMA_PORT)

reference_paragraph_collection = chroma_client.get_or_create_collection(
    name=REFERENCE_PARAGRAPH_COLLECTION_V2,
    dimension=768
)
reference_content_collection = chroma_client.get_or_create_collection(
    name=REFERENCE_CONTENT_COLLECTION_V2,
    dimension=768
)
news_title_collection = chroma_client.get_or_create_collection(
    name=NEWS_TITLE_COLLECTION_V2,
    dimension=768
)
news_content_collection = chroma_client.get_or_create_collection(
    name=NEWS_CONTENT_COLLECTION_V2,
    dimension=768
)
news_paragraph_collection = chroma_client.get_or_create_collection(
    name=NEWS_PARAGRAPH_COLLECTION_V2,
    dimension=768
)


def update_reference_news_embeddings():
    print("[INFO] 참조 뉴스 임베딩 업데이트 시작")
    documents = reference_news_collection.find({})
    for doc in documents:
        news_id = str(doc["_id"])
        title = doc.get("title", "")
        content = doc.get("content", "")
        paragraphs = doc.get("paragraphs", [])

        for idx, paragraph in enumerate(paragraphs):
            embedding = embedding_model.encode(paragraph).tolist()
            doc_id = f"{news_id}_p_{idx}"
            reference_paragraph_collection.upsert(
                ids=[doc_id],
                documents=[paragraph],
                embeddings=[embedding],
                metadatas=[{"news_id": news_id, "type": "paragraph"}]
            )

        if content:
            embedding = embedding_model.encode(content).tolist()
            reference_content_collection.upsert(
                ids=[f"{news_id}_content"],
                documents=[content],
                embeddings=[embedding],
                metadatas=[{"news_id": news_id, "type": "content"}]
            )

    print("[INFO] 참조 뉴스 임베딩 업데이트 완료")


def update_news_articles_embeddings():
    print("[INFO] 뉴스 기사 임베딩 업데이트 시작")
    documents = news_articles_collection.find({})
    for doc in documents:
        news_id = str(doc["_id"])
        title = doc.get("title", "")
        content = doc.get("content", "")
        paragraphs = doc.get("paragraphs", [])

        if title:
            embedding = embedding_model.encode(title).tolist()
            news_title_collection.upsert(
                ids=[f"{news_id}_title"],
                documents=[title],
                embeddings=[embedding],
                metadatas=[{"news_id": news_id, "type": "title"}]
            )

        if content:
            embedding = embedding_model.encode(content).tolist()
            news_content_collection.upsert(
                ids=[f"{news_id}_content"],
                documents=[content],
                embeddings=[embedding],
                metadatas=[{"news_id": news_id, "type": "content"}]
            )

        for idx, paragraph in enumerate(paragraphs):
            embedding = embedding_model.encode(paragraph).tolist()
            doc_id = f"{news_id}_p_{idx}"
            news_paragraph_collection.upsert(
                ids=[doc_id],
                documents=[paragraph],
                embeddings=[embedding],
                metadatas=[{"news_id": news_id, "type": "paragraph"}]
            )

    print("[INFO] 뉴스 기사 임베딩 업데이트 완료")


def update_all_embeddings():
    update_reference_news_embeddings()
    update_news_articles_embeddings()
    print("[INFO] 전체 임베딩 업데이트 완료")


def job():
    update_all_embeddings()


if __name__ == "__main__":
    job()
    schedule.every(24).hours.do(job)

    while True:
        schedule.run_pending()
        time.sleep(60)
