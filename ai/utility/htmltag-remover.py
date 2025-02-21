from pymongo import MongoClient
from bs4 import BeautifulSoup

# MongoDB 연결 설정 (환경에 맞게 수정)
MONGO_URI = "mongodb://d208:rnal2qksghkdlxld!@i12d208.p.ssafy.io:27017/goose?authSource=goose"
DATABASE_NAME = "goose"
COLLECTION_NAME = "news_articles"

client = MongoClient(MONGO_URI)
db = client[DATABASE_NAME]
collection = db[COLLECTION_NAME]

def remove_html_tags(text):
    if text:
        soup = BeautifulSoup(text, "html.parser")
        return soup.get_text()
    return text

# 모든 문서 순회하며 특정 필드 업데이트
for doc in collection.find():
    updated_fields = {}
    
    # title 필드 처리
    if "title" in doc and doc["title"]:
        cleaned_title = remove_html_tags(doc["title"])
        if cleaned_title != doc["title"]:
            updated_fields["title"] = cleaned_title

    # content 필드 처리
    if "content" in doc and doc["content"]:
        cleaned_content = remove_html_tags(doc["content"])
        if cleaned_content != doc["content"]:
            updated_fields["content"] = cleaned_content

    # description 필드 처리
    if "description" in doc and doc["description"]:
        cleaned_description = remove_html_tags(doc["description"])
        if cleaned_description != doc["description"]:
            updated_fields["description"] = cleaned_description

    # 변경사항이 있으면 업데이트
    if updated_fields:
        collection.update_one({"_id": doc["_id"]}, {"$set": updated_fields})
        print(f"Updated document with _id: {doc['_id']}")

print("HTML 태그 제거 및 업데이트 완료!")