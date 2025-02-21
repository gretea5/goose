import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
import yaml
import torch
import gluonnlp as nlp
import kss
from kobert import get_pytorch_kobert_model
from kobert.utils import get_tokenizer

from models import create_model

app = FastAPI()

class NewsInput(BaseModel):
    paragraphs: List[str]

class TextOutput(BaseModel):
    coherence_scores: List[float]
    segment_breaks: List[int]
    sentences: List[str]

@app.on_event("startup")
async def startup_event():
    global model, dataset, cfg
    
    # config 파일 로드
    config_path = os.path.join(
        os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
        'configs',
        'KoBERTSeg',
        'KoBERTSeg-test.yaml'
    )
    
    cfg = yaml.load(
        open(config_path, 'r'),
        Loader=yaml.FullLoader
    )
    
    # KoBERT 모델과 vocab 로드
    _, vocab = get_pytorch_kobert_model(cachedir=".cache")
    tokenizer = nlp.data.BERTSPTokenizer(get_tokenizer(), vocab, lower=False)
    
    # 모델 생성
    model = create_model(
        modelname=cfg['MODEL']['modelname'],
        hparams=cfg['MODEL']['PARAMETERS'],
        tokenizer=tokenizer,
        checkpoint_path=cfg['MODEL']['CHECKPOINT']['checkpoint_path']
    )
    
    # dataset 생성
    dataset = __import__('dataset').__dict__[f"{cfg['DATASET']['name']}Dataset"](
        tokenizer=tokenizer,
        vocab=vocab,
        **cfg['DATASET']['PARAMETERS']
    )
    
    model.eval()
    print("Model and dataset initialized!")

@app.post("/analyze")
async def analyze_coherence(news: NewsInput):
    # KSS를 사용하여 paragraphs를 문장 단위로 분리
    all_sentences = []
    for paragraph in news.paragraphs:
        paragraph_sentences = kss.split_sentences(paragraph)
        all_sentences.extend(paragraph_sentences)
    
    # 입력 데이터 변환
    inputs = dataset.single_preprocessor(doc=all_sentences)
    
    # 모델 예측
    with torch.no_grad():
        outputs = model(**inputs)
        outputs = torch.nn.functional.softmax(outputs, dim=1)
        preds = outputs.argmax(dim=-1)
    
    # 결과 처리
    coherence_scores = []
    segment_breaks = []
    
    for i in range(len(all_sentences)-1):
        coherence_score = float(1 - outputs[i, 1])
        coherence_scores.append(coherence_score)
        if preds[i] == 1:
            segment_breaks.append(i+1)
    
    return TextOutput(
        coherence_scores=coherence_scores,
        segment_breaks=segment_breaks,
        sentences=all_sentences
    )

@app.get("/")
def read_root():
    return {"message": "KoBERTSeg Coherence Analysis API is running!"}

@app.get("/test")
def test():
    return {"status": "running", "service": "Text Coherence Analysis API"}