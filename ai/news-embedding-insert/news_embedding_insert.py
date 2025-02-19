from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
from sentence_transformers import SentenceTransformer
import chromadb

app = FastAPI()

CHROMA_HOST = "i12d208.p.ssafy.io"
CHROMA_PORT = 8000

NEWS_TITLE_COLLECTION_V2 = "news_articles_title_v2"
NEWS_CONTENT_COLLECTION_V2 = "news_articles_content_v2"
NEWS_PARAGRAPH_COLLECTION_V2 = "news_articles_paragraphs_v2"

REFERENCE_PARAGRAPH_COLLECTION_V2 = "reference_paragraphs_v2"
REFERENCE_CONTENT_COLLECTION_V2 = "reference_content_v2"

embedding_model = SentenceTransformer("all-mpnet-base-v2")

chroma_client = chromadb.HttpClient(host=CHROMA_HOST, port=CHROMA_PORT)

title_collection = chroma_client.get_or_create_collection(
    name=NEWS_TITLE_COLLECTION_V2,
    dimension=768
)
content_collection = chroma_client.get_or_create_collection(
    name=NEWS_CONTENT_COLLECTION_V2,
    dimension=768
)
paragraph_collection = chroma_client.get_or_create_collection(
    name=NEWS_PARAGRAPH_COLLECTION_V2,
    dimension=768
)

reference_paragraph_collection = chroma_client.get_or_create_collection(
    name=REFERENCE_PARAGRAPH_COLLECTION_V2,
    dimension=768
)
reference_content_collection = chroma_client.get_or_create_collection(
    name=REFERENCE_CONTENT_COLLECTION_V2,
    dimension=768
)


class NewsArticle(BaseModel):
    id: str
    title: str
    content: str
    paragraphs: List[str]
    pubDate: Optional[str] = None


@app.post("/store-news")
async def store_news(news: NewsArticle):
    title_embedding = embedding_model.encode(news.title).tolist()
    content_embedding = embedding_model.encode(news.content).tolist()

    title_collection.add(ids=[news.id + "_title"], documents=[news.title], embeddings=[title_embedding])
    content_collection.add(ids=[news.id + "_content"], documents=[news.content], embeddings=[content_embedding])

    for i, paragraph in enumerate(news.paragraphs):
        paragraph_embedding = embedding_model.encode(paragraph).tolist()
        paragraph_collection.add(ids=[f"{news.id}_p_{i}"], documents=[paragraph], embeddings=[paragraph_embedding])

    return {"status": "success"}


@app.post("/store-reference-news")
async def store_reference_news(news: NewsArticle):
    content_embedding = embedding_model.encode(news.content).tolist()

    reference_content_collection.add(ids=[news.id + "_content"], documents=[news.content], embeddings=[content_embedding])

    for i, paragraph in enumerate(news.paragraphs):
        paragraph_embedding = embedding_model.encode(paragraph).tolist()
        reference_paragraph_collection.add(ids=[f"{news.id}_p_{i}"], documents=[paragraph], embeddings=[paragraph_embedding])

    return {"status": "success"}
