# ✅ 공식 Python 3.9 이미지 사용
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

# ✅ 필요한 파일 복사
COPY requirements.txt ./
COPY today_hot_keywords.py ./

# ✅ 패키지 설치 (spaCy 모델 제외)
RUN pip install --upgrade pip && pip install -r requirements.txt

# ✅ spaCy 한국어 모델 수동 설치 (Docker & EC2 자동화)
RUN curl -L -o ko_core_news_sm-3.5.0.tar.gz https://github.com/explosion/spacy-models/releases/download/ko_core_news_sm-3.5.0/ko_core_news_sm-3.5.0.tar.gz \
    && pip install ko_core_news_sm-3.5.0.tar.gz \
    && rm ko_core_news_sm-3.5.0.tar.gz

# ✅ FastAPI 실행 (운영환경에서는 --reload 제거)
CMD ["uvicorn", "today_hot_keywords:app", "--host", "0.0.0.0", "--port", "5053"]
