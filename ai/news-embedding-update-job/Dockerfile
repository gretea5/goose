# Python 3.9 기반 이미지 사용
FROM python:3.9

# 작업 디렉토리 생성
WORKDIR /app

# 현재 디렉토리의 모든 파일을 /app에 복사
COPY . /app

# pip 최신 버전으로 업그레이드 후, 의존성 설치
RUN pip install --upgrade pip
RUN pip install -r requirements.txt

# 스케줄러 스크립트 실행 (news_embedding_update.py)
CMD ["python", "news_embedding_update.py"]