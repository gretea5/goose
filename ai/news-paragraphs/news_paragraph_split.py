from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
from transformers import BertTokenizer, BertModel
import torch
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from sentence_transformers import SentenceTransformer
from soynlp.tokenizer import RegexTokenizer

# ✅ FastAPI 앱 생성
app = FastAPI()

# ✅ Sentence-BERT 모델 로드
sbert_model = SentenceTransformer("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2")

# ✅ 형태소 분석기 변경 (soynlp)
tokenizer = RegexTokenizer()

# ✅ 요청 데이터 모델 정의
class ParagraphRequest(BaseModel):
    content: str  # 뉴스 본문 텍스트


def is_connection_word(sentence):
    """ 문장이 접속 부사(혹은 연결어)로 시작하는지 확인 (soynlp 기반) """
    connection_words = {"특히", "그러나", "게다가", "또한", "이와 함께", "한편", "더불어", "이에 따라", "결과적으로", "즉", "이는"}
    words = tokenizer.tokenize(sentence)
    
    return bool(words and words[0] in connection_words)


def split_sentences_by_period(text):
    """ 문장을 온점('.') 기준으로 나눠 리스트로 반환 """
    sentences = [s.strip() for s in text.split('.') if s.strip()]
    return [s + '.' for s in sentences]  # 각 문장 끝에 '.' 유지


def get_sentence_embeddings(text):
    """ KoBERT를 활용하여 문장의 임베딩 벡터를 얻음 """
    tokenizer = BertTokenizer.from_pretrained("monologg/kobert")
    model = BertModel.from_pretrained("monologg/kobert")

    sentences = split_sentences_by_period(text)
    embeddings = []

    for sent in sentences:
        tokens = tokenizer(sent, return_tensors="pt", padding=True, truncation=True)
        with torch.no_grad():
            outputs = model(**tokens)
        embeddings.append(outputs.last_hidden_state.mean(dim=1).numpy())

    return sentences, np.array(embeddings).squeeze()


def split_paragraphs_bert(text, threshold=0.9):
    """ 문장 간 의미적 유사도를 측정하여 문단을 구분 """
    sentences, embeddings = get_sentence_embeddings(text)
    similarities = cosine_similarity(embeddings)

    paragraphs = []
    current_paragraph = [sentences[0]]

    for i in range(1, len(sentences)):
        if similarities[i - 1][i] < threshold:  # 유사도가 낮으면 새로운 문단 시작
            paragraphs.append(" ".join(current_paragraph))
            current_paragraph = [sentences[i]]
        else:
            current_paragraph.append(sentences[i])

    if current_paragraph:
        paragraphs.append(" ".join(current_paragraph))

    return paragraphs


def merge_short_paragraphs(paragraphs, min_length=15):
    """ 너무 짧은 문단을 앞 문단과 합치는 함수 """
    fixed_paragraphs = []
    for paragraph in paragraphs:
        if len(paragraph) < min_length and fixed_paragraphs:
            fixed_paragraphs[-1] += " " + paragraph
        else:
            fixed_paragraphs.append(paragraph)

    return fixed_paragraphs


def merge_based_on_similarity(paragraphs, threshold=0.85):
    """ 문맥이 유사한 문단을 하나로 합치는 함수 (Sentence-BERT 기반) """
    if len(paragraphs) < 2:
        return paragraphs

    vectors = sbert_model.encode(paragraphs)
    merged_paragraphs = [paragraphs[0]]

    for i in range(1, len(paragraphs)):
        similarity = cosine_similarity([vectors[i - 1]], [vectors[i]])[0][0]
        if similarity > threshold or is_connection_word(paragraphs[i]):
            merged_paragraphs[-1] += " " + paragraphs[i]
        else:
            merged_paragraphs.append(paragraphs[i])

    return merged_paragraphs


def merge_based_on_naturalness(paragraphs, threshold=0.85):
    """ 문맥적 자연스러움을 고려하여 문장을 병합하는 함수 """
    if len(paragraphs) < 2:
        return paragraphs

    merged_paragraphs = [paragraphs[0]]

    for i in range(1, len(paragraphs)):
        combined_text = merged_paragraphs[-1] + " " + paragraphs[i]
        original_vector = sbert_model.encode([paragraphs[i]])
        combined_vector = sbert_model.encode([combined_text])
        similarity = cosine_similarity(original_vector, combined_vector)[0][0]

        if similarity > threshold:
            merged_paragraphs[-1] += " " + paragraphs[i]
        else:
            merged_paragraphs.append(paragraphs[i])

    return merged_paragraphs


def final_merge_paragraphs(paragraphs, threshold=0.85, min_length=15):
    """ 문맥적 유사도, 문장 자연스러움, 문장 길이를 고려한 최적의 병합 방식 """
    paragraphs = merge_short_paragraphs(paragraphs, min_length=min_length)
    paragraphs = merge_based_on_similarity(paragraphs, threshold=threshold)
    paragraphs = merge_based_on_naturalness(paragraphs, threshold=threshold)

    return paragraphs


@app.get("/health")
async def health_check():
    """ 서버 상태 체크 API """
    return {"status": "OK"}


@app.post("/split-paragraphs")
async def split_paragraphs(request: ParagraphRequest):
    """ 뉴스 본문을 문단으로 나누는 API """
    try:
        text = request.content.strip()
        if not text:
            raise HTTPException(status_code=400, detail="뉴스 본문이 비어 있습니다.")

        paragraphs = split_paragraphs_bert(text)
        final_paragraphs = final_merge_paragraphs(paragraphs, threshold=0.85, min_length=15)

        return {"paragraphs": final_paragraphs}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ✅ FastAPI 실행 (Docker 환경에서는 `uvicorn` 사용)
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5053, reload=True)
