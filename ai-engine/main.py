"""AuthGuard AI Risk Engine.

Pontuação de risco baseada em contexto (mock atual; substituir por modelo ML).
Schema tipado, rate-limit por IP, CORS restritivo, /health pra liveness probe.
"""

from __future__ import annotations

import logging
import os
import secrets
from typing import List, Optional

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, EmailStr, Field
from pythonjsonlogger import jsonlogger
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded
from slowapi.util import get_remote_address

logger = logging.getLogger("authguard.risk")
handler = logging.StreamHandler()
handler.setFormatter(jsonlogger.JsonFormatter("%(asctime)s %(levelname)s %(name)s %(message)s"))
logger.addHandler(handler)
logger.setLevel(os.getenv("LOG_LEVEL", "INFO"))

limiter = Limiter(key_func=get_remote_address, default_limits=["30/minute"])

app = FastAPI(title="AuthGuard Risk Engine", version="0.2.0")
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

allowed_origins = [o.strip() for o in os.getenv("ALLOWED_ORIGINS", "http://localhost:8080").split(",") if o.strip()]
app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed_origins,
    allow_methods=["POST", "GET"],
    allow_headers=["Content-Type"],
    allow_credentials=False,
    max_age=3600,
)

_RNG = secrets.SystemRandom()


class RiskAnalysisRequest(BaseModel):
    account: EmailStr
    ip: Optional[str] = Field(None, max_length=64)
    device_fp: Optional[str] = Field(None, max_length=128)
    user_agent: Optional[str] = Field(None, max_length=512)


class RiskAnalysisResponse(BaseModel):
    risk_score: float
    high_risk: bool
    anomalies: List[str]


@app.get("/health")
async def health() -> dict:
    return {"status": "UP"}


@app.post("/analyze", response_model=RiskAnalysisResponse)
@limiter.limit("30/minute")
async def analyze_risk(request: Request, payload: RiskAnalysisRequest) -> RiskAnalysisResponse:
    # TODO: substituir por modelo ML (device fingerprint, typing cadence, IP geo, history)
    risk_score = _RNG.uniform(0, 100)
    high_risk = risk_score > 80
    anomalies = ["unusual_ip"] if high_risk else []
    logger.info(
        "risk_assessed",
        extra={"account": payload.account, "risk_score": round(risk_score, 2), "high_risk": high_risk},
    )
    return RiskAnalysisResponse(risk_score=risk_score, high_risk=high_risk, anomalies=anomalies)


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "main:app",
        host=os.getenv("HOST", "0.0.0.0"),
        port=int(os.getenv("PORT", "8000")),
        workers=int(os.getenv("WORKERS", "1")),
        proxy_headers=True,
        forwarded_allow_ips=os.getenv("FORWARDED_ALLOW_IPS", "127.0.0.1"),
    )
