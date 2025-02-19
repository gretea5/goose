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
CHROMA_PORT = 8001

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
    name=REFERENCE_PARAGRAPH_COLLECTION_V2
)
reference_content_collection = chroma_client.get_or_create_collection(
    name=REFERENCE_CONTENT_COLLECTION_V2
)
news_title_collection = chroma_client.get_or_create_collection(
    name=NEWS_TITLE_COLLECTION_V2
)
news_content_collection = chroma_client.get_or_create_collection(
    name=NEWS_CONTENT_COLLECTION_V2
)
news_paragraph_collection = chroma_client.get_or_create_collection(
    name=NEWS_PARAGRAPH_COLLECTION_V2
)



def update_reference_news_embeddings():
    print("[INFO] 참조 뉴스 임베딩 업데이트 시작")

    existing_paragraph_data = reference_paragraph_collection.get(include=["documents", "embeddings", "metadatas"])
    existing_content_data = reference_content_collection.get(include=["documents", "embeddings", "metadatas"])

    existing_paragraph_ids = set(existing_paragraph_data["ids"])
    existing_content_ids = set(existing_content_data["ids"])

    # 임베딩이 None이거나 누락된 ID 찾기
    paragraph_ids_to_update = set(
        doc_id for doc_id, emb in zip(existing_paragraph_data["ids"], existing_paragraph_data["embeddings"]) if emb is None
    )
    content_ids_to_update = set(
        doc_id for doc_id, emb in zip(existing_content_data["ids"], existing_content_data["embeddings"]) if emb is None
    )

    documents = reference_news_collection.find({})
    for doc in documents:
        news_id = str(doc["_id"])
        content = doc.get("content", "")
        paragraphs = doc.get("paragraphs", [])

        for idx, paragraph in enumerate(paragraphs):
            doc_id = f"{news_id}_p_{idx}"
            if doc_id not in existing_paragraph_ids or doc_id in paragraph_ids_to_update:
                embedding = embedding_model.encode(paragraph).tolist()
                reference_paragraph_collection.upsert(
                    ids=[doc_id],
                    documents=[paragraph],
                    embeddings=[embedding],
                    metadatas=[{"news_id": news_id, "type": "paragraph"}]
                )

        if content:
            content_id = f"{news_id}_content"
            if content_id not in existing_content_ids or content_id in content_ids_to_update:
                embedding = embedding_model.encode(content).tolist()
                reference_content_collection.upsert(
                    ids=[content_id],
                    documents=[content],
                    embeddings=[embedding],
                    metadatas=[{"news_id": news_id, "type": "content"}]
                )

    print("[INFO] 참조 뉴스 임베딩 업데이트 완료")





def update_news_articles_embeddings():
    print("[INFO] 뉴스 기사 임베딩 업데이트 시작")

    existing_title_data = news_title_collection.get(include=["documents", "embeddings", "metadatas"])
    existing_content_data = news_content_collection.get(include=["documents", "embeddings", "metadatas"])
    existing_paragraph_data = news_paragraph_collection.get(include=["documents", "embeddings", "metadatas"])

    existing_title_ids = set(existing_title_data["ids"])
    existing_content_ids = set(existing_content_data["ids"])
    existing_paragraph_ids = set(existing_paragraph_data["ids"])

    # 임베딩이 None이거나 누락된 ID 찾기
    title_ids_to_update = set(
        doc_id for doc_id, emb in zip(existing_title_data["ids"], existing_title_data["embeddings"]) if emb is None
    )
    content_ids_to_update = set(
        doc_id for doc_id, emb in zip(existing_content_data["ids"], existing_content_data["embeddings"]) if emb is None
    )
    paragraph_ids_to_update = set(
        doc_id for doc_id, emb in zip(existing_paragraph_data["ids"], existing_paragraph_data["embeddings"]) if emb is None
    )

    documents = news_articles_collection.find({})
    for doc in documents:
        news_id = str(doc["_id"])
        title = doc.get("title", "")
        content = doc.get("content", "")
        paragraphs = doc.get("paragraphs", [])

        if title:
            title_id = f"{news_id}_title"
            if title_id not in existing_title_ids or title_id in title_ids_to_update:
                embedding = embedding_model.encode(title).tolist()
                news_title_collection.upsert(
                    ids=[title_id],
                    documents=[title],
                    embeddings=[embedding],
                    metadatas=[{"news_id": news_id, "type": "title"}]
                )

        if content:
            content_id = f"{news_id}_content"
            if content_id not in existing_content_ids or content_id in content_ids_to_update:
                embedding = embedding_model.encode(content).tolist()
                news_content_collection.upsert(
                    ids=[content_id],
                    documents=[content],
                    embeddings=[embedding],
                    metadatas=[{"news_id": news_id, "type": "content"}]
                )

        for idx, paragraph in enumerate(paragraphs):
            paragraph_id = f"{news_id}_p_{idx}"
            if paragraph_id not in existing_paragraph_ids or paragraph_id in paragraph_ids_to_update:
                embedding = embedding_model.encode(paragraph).tolist()
                news_paragraph_collection.upsert(
                    ids=[paragraph_id],
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
