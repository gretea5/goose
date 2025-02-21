# ✅ Python 3.9 기반 Docker 이미지 사용
FROM python:3.9

# ✅ 작업 디렉토리 설정
WORKDIR /app

# ✅ 시스템 패키지 업데이트 및 JDK 17 설치
RUN apt update && apt install -y \
    openjdk-17-jdk \
    g++ \
    gcc \
    make \
    curl \
    && apt clean

# ✅ JAVA_HOME 환경 변수 설정 (JDK 17)
ENV JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# ✅ Python 패키지 의존성 설치
COPY requirements.txt /app/requirements.txt
RUN pip install --upgrade pip
RUN pip install -r requirements.txt

# ✅ spaCy 한국어 모델 설치
RUN python -m spacy download ko_core_news_sm

# ✅ 작업 디렉토리에 모든 소스 복사
COPY . /app

# ✅ FastAPI 서버 실행
CMD ["uvicorn", "content_keyword:app", "--host", "0.0.0.0", "--port", "6050"]
