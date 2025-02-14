# Python 3.9 기반 이미지 사용
FROM python:3.9

# Rust 설치: rustup을 통해 설치하고, PATH 환경변수를 업데이트
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y && \
    . $HOME/.cargo/env && \
    echo 'export PATH="$HOME/.cargo/bin:$PATH"' >> ~/.bashrc

# 작업 디렉토리 생성
WORKDIR /app

# 필요한 파일 복사
COPY . /app

# 최신 pip 업그레이드 및 의존성 설치
RUN pip install --upgrade pip
RUN pip install -r requirements.txt

# FastAPI 애플리케이션 실행 (uvicorn) - 포트 5061
CMD ["uvicorn", "news_paragraph_reliability:app", "--host", "0.0.0.0", "--port", "5061"]
