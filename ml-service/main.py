from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional, Dict
import numpy as np
from models.categorizer import TransactionCategorizer
from models.pattern_analyzer import SpendingPatternAnalyzer
from models.forecaster import ExpenseForecaster
from preprocessing.data_cleaner import clean_description
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Personal Finance ML Service",
    description="Machine Learning analytics for personal finance management",
    version="1.0.0"
)

# FIXED: snake_case method name
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize ML models
categorizer = TransactionCategorizer()
pattern_analyzer = SpendingPatternAnalyzer()
forecaster = ExpenseForecaster()

# Request/Response Models
class CategorizationRequest(BaseModel):
    description: str
    amount: float
    merchant_name: Optional[str] = None
    transaction_date: Optional[str] = None

class CategorizationResponse(BaseModel):
    category: str
    confidence: float
    subcategory: Optional[str] = None

class PatternAnalysisRequest(BaseModel):
    user_id: str
    transactions: List[Dict]
    period: Optional[str] = None

class PatternAnalysisResponse(BaseModel):
    patterns: List[Dict]
    insights: List[str]
    recommendations: List[str]

class ForecastRequest(BaseModel):
    user_id: str
    historical_spending: List[Dict]
    period: str = "next_month"

class ForecastResponse(BaseModel):
    predicted_expenses: Dict[str, float]
    total_predicted: float
    confidence: float
    recommendation: str

@app.get("/")
async def root():
    return {
        "service": "Personal Finance ML Service",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "health": "/health",
            "docs": "/docs",
            "categorize": "/api/v1/ml/categorize-transaction",
            "analyze_patterns": "/api/v1/ml/analyze-patterns",
            "predict_expense": "/api/v1/ml/predict-expense"
        }
    }

@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "ML Analytics Service"}

@app.post("/api/v1/ml/categorize-transaction", response_model=CategorizationResponse)
async def categorize_transaction(request: CategorizationRequest):
    try:
        logger.info(f"Categorizing: {request.description}")
        cleaned = clean_description(request.description)
        category, confidence = categorizer.predict(cleaned, request.amount)

        return CategorizationResponse(
            category=category,
            confidence=float(confidence),
            subcategory=None
        )
    except Exception as e:
        logger.error(f"Categorization error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/v1/ml/analyze-patterns", response_model=PatternAnalysisResponse)
async def analyze_patterns(request: PatternAnalysisRequest):
    try:
        if not request.transactions:
            return PatternAnalysisResponse(
                patterns=[],
                insights=["Not enough data to analyze patterns"],
                recommendations=["Add more transactions for better insights"]
            )

        patterns, insights, recommendations = pattern_analyzer.analyze(
            request.transactions
        )

        return PatternAnalysisResponse(
            patterns=patterns,
            insights=insights,
            recommendations=recommendations
        )
    except Exception as e:
        logger.error(f"Pattern analysis error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/v1/ml/predict-expense", response_model=ForecastResponse)
async def predict_expense(request: ForecastRequest):
    try:
        if not request.historical_spending:
            return ForecastResponse(
                predicted_expenses={},
                total_predicted=0.0,
                confidence=0.0,
                recommendation="Not enough historical data for prediction"
            )

        predictions, total, confidence = forecaster.predict(
            request.historical_spending
        )

        return ForecastResponse(
            predicted_expenses=predictions,
            total_predicted=float(total),
            confidence=float(confidence),
            recommendation=f"Based on {len(request.historical_spending)} months of data"
        )
    except Exception as e:
        logger.error(f"Forecast error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)