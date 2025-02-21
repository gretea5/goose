from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import requests
from bs4 import BeautifulSoup
import logging

# FastAPI 인스턴스 생성
app = FastAPI()

# 로깅 설정
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

# 요청 데이터 모델 정의
class NewsRequest(BaseModel):
    url: str

def extract_news_content(url): 
    """ HTML을 직접 파싱하여 뉴스 본문을 추출하는 함수 """
    headers = {
        "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
    }

    try:
        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()

        soup = BeautifulSoup(response.text, 'html.parser')

        # ✅ 네이버 뉴스 본문 추출 예제
        article_body = soup.select_one("#dic_area, #articleBodyContents")  # 네이버 뉴스 및 일반적인 기사 본문
        article_text = article_body.get_text(separator="\n", strip=True) if article_body else "본문 없음"

        # ✅ 대표 이미지 추출
        image_tag = soup.select_one("meta[property='og:image']")
        top_image = image_tag["content"] if image_tag else None

        return {
            "title": soup.title.string if soup.title else "제목 없음",
            "text": article_text,
            "image": top_image
        }

    except Exception as e:
        logging.error(f"❌ [BeautifulSoup] 크롤링 실패: {url} | 오류: {str(e)}")
        return None

@app.post("/extract")
async def extract_news(request: NewsRequest):
    logging.info(f"🔍 [FastAPI] 크롤링 요청: {request.url}")

    result = extract_news_content(request.url)
    
    if result:
        logging.info(f"✅ [FastAPI] 크롤링 완료 - 제목: {result['title']}")
        logging.info(f"✅ [FastAPI] 본문 길이: {len(result['text'])}")
        logging.info(f"✅ [FastAPI] 대표 이미지: {result['image']}")
        return result
    else:
        raise HTTPException(status_code=500, detail="크롤링 실패")

