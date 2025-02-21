from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
from bareunpy import Corrector

# Corrector 초기화
API_KEY = "koba-ERQULJY-OSZEAPQ-R5FBH4I-IW6CGBQ"  # 본인의 API 키를 입력하세요
HOST = "i12d208.p.ssafy.io"        # 로컬 서버를 사용하는 경우
PORT = 5757               # 도커로 설치한 경우 5757로 호출
corrector = Corrector(apikey=API_KEY, host=HOST, port=PORT)

app = FastAPI(title="BareunPy Correction API")

# 요청 Body 모델 정의
class SentenceRequest(BaseModel):
    content: str

class SentenceListRequest(BaseModel):
    contents: List[str]

@app.post("/correct")
def correct_sentence(request: SentenceRequest):
    """
    단일 문장 교정 엔드포인트
    """
    response = corrector.correct_error(content=request.content, auto_split=True)
    return {"origin": response.origin, "revised": response.revised}

@app.post("/correct_list")
def correct_sentences(request: SentenceListRequest):
    """
    여러 문장 교정 엔드포인트
    """
    responses = corrector.correct_error_list(contents=request.contents, auto_split=True)
    results = [{"origin": res.origin, "revised": res.revised} for res in responses]
    return results

# 개발 모드 실행 시
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=5060, reload=True)
