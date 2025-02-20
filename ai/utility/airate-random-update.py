from pymongo import MongoClient
import random

# MongoDB 연결 (환경에 맞게 수정)
MONGO_URI = "mongodb://d208:rnal2qksghkdlxld!@i12d208.p.ssafy.io:27017/goose?authSource=goose"
client = MongoClient(MONGO_URI)
db = client["goose"]
collection = db["news_articles"]

# aiRate가 null이거나 없는 문서 찾기
query = {
    "$or": [
        {"aiRate": None},
        {"aiRate": {"$exists": False}}
    ]
}

# 각 문서에 랜덤값(0~10) 설정
for doc in collection.find(query):
    random_value = round(random.uniform(0, 10), 2)  # 소수점 둘째 자리까지
    collection.update_one(
        {"_id": doc["_id"]},
        {"$set": {"aiRate": random_value}}
    )
    print(f"Updated {doc['_id']} with aiRate: {random_value}")

print("랜덤 값 업데이트 완료!")
