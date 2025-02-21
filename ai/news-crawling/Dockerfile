# Python 3.9 기반 이미지 사용
FROM python:3.9

# 작업 디렉토리 생성
WORKDIR /app

# 필요한 파일 복사
COPY . /app

# 가상 환경 활성화 및 의존성 설치
RUN pip install --upgrade pip
RUN pip install -r requirements.txt

# FastAPI 애플리케이션 실행 (uvicorn)
CMD ["uvicorn", "news_crawling:app", "--host", "0.0.0.0", "--port", "5050"]