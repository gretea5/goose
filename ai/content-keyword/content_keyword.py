from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import re
from konlpy.tag import Okt
import spacy
from collections import Counter
from fastapi.middleware.cors import CORSMiddleware

# ✅ FastAPI 서버 초기화
app = FastAPI()

# CORS 설정 추가
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 모든 도메인 허용 (보안 필요시 특정 도메인으로 변경)
    allow_credentials=True,
    allow_methods=["*"],  # 모든 HTTP 메서드 허용 (GET, POST 등)
    allow_headers=["*"],  # 모든 HTTP 헤더 허용
)

# ✅ 한국어 자연어처리 엔진 로드 (KoNLPy + spaCy)
okt = Okt()
try:
    nlp = spacy.load("ko_core_news_sm")  # 한국어 NLP 모델
except Exception:
    raise RuntimeError("❌ spaCy 'ko_core_news_sm' 모델이 설치되지 않았습니다. 설치 후 다시 실행하세요.")

# ✅ 불용어 리스트 병합 (KoNLPy + spaCy + Custom)
konlpy_stopwords = {"의", "가", "이", "은", "들", "는", "좀", "잘", "걍", "과", "도", "를", "으로", "자", "에", "와", "한", "하다"}
custom_stopwords = {"뉴스", "기사", "사진", "출처", "연합뉴스", "등", "대한", "및", "하는", "그", "이", "저", "등등", "...", "‘", "’", "“", "”"}

# ✅ spaCy 한국어 불용어 예외 처리
try:
    spacy_stopwords = set(nlp.Defaults.stop_words)  # spaCy 한국어 불용어
except AttributeError:
    spacy_stopwords = set()

# ✅ 최종 불용어 리스트 병합
stopwords_set = konlpy_stopwords | spacy_stopwords | custom_stopwords

class KeywordRequest(BaseModel):
    text: str  # titles -> text로 변경

def clean_text(text: str) -> str:
    """ 🔹 텍스트에서 HTML 엔티티, 특수문자, 줄바꿈 문자 제거 """
    text = re.sub(r"&quot;|&amp;|&lt;|&gt;", "", text)  # HTML 엔티티 제거
    text = re.sub(r"\n", " ", text)  # 줄바꿈 문자(\n) 공백으로 변경
    text = re.sub(r"[^가-힣a-zA-Z0-9\s]", "", text)  # 특수문자 제거
    return text.strip()

@app.post("/content_keywords")
async def extract_keywords_nltk(request: KeywordRequest):
    try:
        # ✅ 요청에서 받은 text만 사용
        full_text = clean_text(request.text)

        print("🔹 [OCR 텍스트 변환 완료] full_text:", full_text)

        # ✅ 토큰화 (KoNLPy + spaCy)
        tokens = okt.morphs(full_text) + [token.text for token in nlp(full_text)]

        # ✅ 불용어 제거 및 명사 필터링
        keywords = [word for word in tokens if word not in stopwords_set and len(word) > 1]

        # ✅ 빈도수 기반 상위 3개 키워드 선택
        keyword_counts = Counter(keywords)
        top_keywords = [word for word, count in keyword_counts.most_common(3)]
        print("🔹 [추출된 키워드] top_keywords:", top_keywords)

        return {"keywords": top_keywords}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
