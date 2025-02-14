from fastapi import FastAPI, HTTPException
from pydantic import BaseModel


app = FastAPI()

class titleAnalysisRequest(BaseModel):
    newsId: str
    referenceNewsId: str

@app.post("/title-compare-contents")
async def title_bias_analyse(request: titleAnalysisRequest):
    

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
