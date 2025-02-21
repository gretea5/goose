# ✅ 공식 Python 3.9 이미지 사용
FROM python:3.9

# ✅ 작업 디렉토리 생성
WORKDIR /app

# ✅ 필요한 파일 복사
COPY . /app

# ✅ 패키지 설치
RUN pip install --upgrade pip
RUN pip install -r requirements.txt

# ✅ FastAPI 실행 (운영환경에서는 --reload 제거)
CMD ["uvicorn", "korean_spelling_corrector:app", "--host", "0.0.0.0", "--port", "5060"]
