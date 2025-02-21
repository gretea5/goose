from pymongo import MongoClient

MONGO_URI = "mongodb://d208:rnal2qksghkdlxld!@i12d208.p.ssafy.io:27017/goose?authSource=goose"
DATABASE_NAME = "goose"
COLLECTION_NAME = "news_articles"

client = MongoClient(MONGO_URI)
db = client[DATABASE_NAME]
collection = db[COLLECTION_NAME]

# 1. 중복 title 찾기
pipeline = [
    {"$group": {"_id": "$title", "ids": {"$push": "$_id"}, "count": {"$sum": 1}}},
    {"$match": {"count": {"$gt": 1}}}
]

duplicates = collection.aggregate(pipeline)

to_delete = []

# 2. 중복 데이터 확인 및 삭제 목록 준비
for doc in duplicates:
    title = doc['_id']
    ids = doc['ids']
    print(f"[Title] {title}")
    print(f"[Document IDs] {ids}")
    print("-" * 50)
    
    # 첫 번째 데이터만 남기고 나머지는 삭제 예정
    to_delete.extend(ids[1:])

print(f"총 {len(to_delete)}개의 중복 문서가 삭제될 예정입니다.")

# 3. 삭제 여부 확인 후 실행
confirm = input("정말 삭제하시겠습니까? (yes/no): ").strip().lower()
if confirm == 'yes':
    result = collection.delete_many({"_id": {"$in": to_delete}})
    print(f"{result.deleted_count}개의 문서가 삭제되었습니다.")
else:
    print("삭제 작업이 취소되었습니다.")
