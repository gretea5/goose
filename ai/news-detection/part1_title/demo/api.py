import sys
import os
import gc
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional, Union
import yaml
import torch

from dataset import create_tokenizer
from models import create_model

app = FastAPI()

# 서버 시작 시 모델과 데이터셋 초기화
@app.on_event("startup")
async def startup_event():
   global model, dataset, tokenizer, cfg
   
   # config 파일 로드
   config_path = os.path.join(
       os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
       'configs',
       'BERT',
       'BERT-test.yaml'
   )
   
   cfg = yaml.load(
       open(config_path, 'r'),
       Loader=yaml.FullLoader
   )
   
   # tokenizer 생성
   tokenizer, word_embed = create_tokenizer(
       name=cfg['TOKENIZER']['name'],
       vocab_path=cfg['TOKENIZER'].get('vocab_path', None),
       max_vocab_size=cfg['TOKENIZER'].get('max_vocab_size', None)
   )
   
   # 모델 생성
   model = create_model(
       modelname=cfg['MODEL']['modelname'],
       hparams=cfg['MODEL']['PARAMETERS'],
       word_embed=word_embed,
       tokenizer=tokenizer,
       freeze_word_embed=cfg['MODEL'].get('freeze_word_embed', False),
       use_pretrained_word_embed=cfg['MODEL'].get('use_pretrained_word_embed', False),
       checkpoint_path=cfg['MODEL']['CHECKPOINT']['checkpoint_path'],
   )
   
   # 모델을 평가 모드로 설정
   model.eval()
   
   # 메모리 정리
   gc.collect()
   
   # dataset 생성
   dataset = __import__('dataset').__dict__[f'{cfg["DATASET"]["name"]}Dataset'](
       tokenizer=tokenizer,
       **cfg["DATASET"]['PARAMETERS']
   )
   
   print("Model and dataset initialized!")

class NewsInput(BaseModel):
   title: str
   paragraphs: List[str]
   detailed_analysis: Optional[bool] = True

class PredictionOutput(BaseModel):
   confidence: float
   paragraph_scores: List[float]

def _get_mask(start_idx, input_ids, sent_ids):
   mask = torch.zeros_like(input_ids).to(torch.bool)
   
   if len(input_ids[start_idx:]) < len(sent_ids):
       mask[start_idx:] = True
       return mask
   
   for i in range(start_idx, len(input_ids) - len(sent_ids)):    
       match = input_ids[i:i+len(sent_ids)] == sent_ids
       if match.sum() == len(sent_ids):        
           mask[i:i+len(sent_ids)] = True
           break
   return mask

def get_sentences_mask(input_ids, sents, encode_func):
   sents_mask = []
   start_idx = 0
   for sent in sents:
       mask = _get_mask(
           start_idx=start_idx,
           input_ids=input_ids,
           sent_ids=torch.tensor(encode_func(sent))
       )
       sents_mask.append(mask)
       
       check = torch.where(mask == True)[0]
       if check.sum() == 0:
           break
       else:
           start_idx = torch.where(mask == True)[0][-1] + 1
   return sents_mask

@app.post("/predict")
async def predict(news: NewsInput):
   # 메모리 확보
   gc.collect()
   
   # 입력 데이터 변환
   inputs = dataset.transform(
       title=news.title,
       text=news.paragraphs
   )
   
   # 모델 예측
   with torch.no_grad():
       outputs = model(**dict([(k,v.unsqueeze(0)) for k,v in inputs.items()])).detach()[0]
       outputs = torch.nn.functional.softmax(outputs, dim=-1)
       pred = outputs.argmax(dim=-1)
       prediction = float(outputs[pred])

       # 빠른 결과 반환 옵션 또는 단일 문단 처리를 위한 간소화
       if not news.detailed_analysis or len(news.paragraphs) <= 1:
           # 간소화된 점수 계산
           para_score = [1.0/len(news.paragraphs)] * len(news.paragraphs) if len(news.paragraphs) > 0 else []
           return PredictionOutput(
               confidence=(1 - prediction) if pred == 1 else prediction,
               paragraph_scores=para_score
           )

       # 메모리 정리
       gc.collect()

       # BERT 문단별 중요도 계산
       def encode_func(src):
           return [tokenizer.convert_tokens_to_ids(s) for s in tokenizer(src)]
           
       layer = model.bert.embeddings.word_embeddings
       refer_token_idx = tokenizer.vocab.token_to_idx['PAD']

       # Attribution 계산 최적화
       from captum.attr import LayerIntegratedGradients, TokenReferenceBase
       token_reference = TokenReferenceBase(reference_token_idx=refer_token_idx)
       attr = LayerIntegratedGradients(model, layer)

       reference_indices = token_reference.generate_reference(
           sequence_length=cfg['DATASET']['PARAMETERS']['max_word_len'],
           device='cpu'
       ).unsqueeze(0)

       # 속도와 정확도 균형을 위한 n_steps 최적화
       attr_score, _ = attr.attribute(
           inputs=inputs['input_ids'].unsqueeze(0),
           baselines=reference_indices,
           target=pred,
           n_steps=3,  # 균형있는 n_steps 값
           return_convergence_delta=True
       )

       # 메모리 효율적인 처리
       attr_score = attr_score.squeeze().sum(dim=-1)

       # 문단별 마스크 생성
       para_mask = get_sentences_mask(
           input_ids=inputs['input_ids'],
           sents=news.paragraphs,
           encode_func=encode_func
       )

       # 문단별 점수 계산
       para_score = []
       for mask in para_mask:
           score = attr_score[mask]
           pos_mask = score > 0
           score = score[pos_mask].sum().item()
           para_score.append(score)
       
       # 점수 정규화
       para_score = torch.tensor(para_score)
       if para_score.sum() > 0:
           para_score = para_score / para_score.sum()
       else:
           para_score = torch.ones(len(news.paragraphs)) / len(news.paragraphs)
   
   # 메모리 정리
   gc.collect()
   
   return PredictionOutput(
       confidence=(1 - prediction) if pred == 1 else prediction,
       paragraph_scores=para_score.tolist()
   )

@app.get("/")
def read_root():
   return {"message": "Hello! API is working!"}

@app.get("/test")
def test():
   return {"status": "running", "service": "Fake News Detection API"}