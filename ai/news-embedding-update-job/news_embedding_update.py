#!/usr/bin/env python
import time
import schedule
from pymongo import MongoClient
from sentence_transformers import SentenceTransformer
import chromadb
import chromadb.config
from chromadb.config import Settings
import numpy as np

# MongoDB 연결 정보
MONGO_URI = os.environ.get("MONGO_URI", "")
DATABASE_NAME = "goose"
COLLECTION_NAME = "reference_news"

# ChromaDB 연결 정보
CHROMA_HOST = "i12d208.p.ssafy.io"
CHROMA_PORT = 8000
CHROMA_COLLECTION_NAME = "reference_paragraphs"

# 임베딩 계산을 위한 SentenceTransformer 모델 로드
embedding_model = SentenceTransformer("all-MiniLM-L6-v2")

# MongoDB 연결
mongo_client = MongoClient(MONGO_URI)
db = mongo_client[DATABASE_NAME]
reference_news_collection = db[COLLECTION_NAME]

# ChromaDB 클라이언트 생성 및 컬렉션 가져오기
chroma_client = chromadb.HttpClient(host=CHROMA_HOST, port=CHROMA_PORT)
chroma_collection = chroma_client.get_or_create_collection(name=CHROMA_COLLECTION_NAME)


def update_chroma_embeddings():
    print("=== ChromaDB 임베딩 업데이트 시작 ===")
    # MongoDB에서 모든 뉴스 문서를 가져옴
    documents = list(reference_news_collection.find({}))
    for doc in documents:
        # 각 문서에서 문단 리스트 가져오기 (예: "paragraphs" 필드가 List[str]임)
        paragraphs = doc.get("paragraphs", [])
        news_id = str(doc.get("_id"))
        title = doc.get("title", "")
        pubDate = doc.get("pubDate", "")
        
        for idx, paragraph in enumerate(paragraphs):
            # 문단 임베딩 계산
            embedding = embedding_model.encode(paragraph).tolist()
            # 고유 문서 ID 생성 (예: 뉴스ID와 문단 인덱스 결합)
            doc_id = f"{news_id}_{idx}"
            metadata = {
                "news_id": news_id,
                "title": title,
                "pubDate": pubDate
            }
            # ChromaDB에 문서 추가 (이미 존재하면 업데이트 로직을 추가할 수 있음)
            try:
                chroma_collection.add(
                    ids=[doc_id],
                    documents=[paragraph],
                    embeddings=[embedding],
                    metadatas=[metadata]
                )
            except Exception as e:
                print(f"문단 저장 실패 (doc_id: {doc_id}): {e}")
    print("=== ChromaDB 임베딩 업데이트 완료 ===")

def job():
    update_chroma_embeddings()

if __name__ == "__main__":
    print("임베딩 업데이트 스케줄러 시작...")
    # 실행 즉시 한 번 작업 수행
    job()
    # 이후 24시간마다 실행
    schedule.every(24).hours.do(job)
    while True:
        schedule.run_pending()
        time.sleep(60)  # 1분마다 스케줄 체크
