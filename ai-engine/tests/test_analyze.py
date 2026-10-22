import sys
from pathlib import Path

import pytest
from fastapi.testclient import TestClient

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from main import app  # noqa: E402


@pytest.fixture()
def client():
    return TestClient(app)


def test_health(client):
    resp = client.get("/health")
    assert resp.status_code == 200
    assert resp.json() == {"status": "UP"}


def test_analyze_valid(client):
    resp = client.post(
        "/analyze",
        json={"account": "user@example.com", "ip": "203.0.113.5", "user_agent": "pytest"},
    )
    assert resp.status_code == 200
    body = resp.json()
    assert 0 <= body["risk_score"] <= 100
    assert isinstance(body["high_risk"], bool)
    assert isinstance(body["anomalies"], list)


def test_analyze_invalid_email(client):
    resp = client.post("/analyze", json={"account": "not-an-email"})
    assert resp.status_code == 422


def test_analyze_missing_account(client):
    resp = client.post("/analyze", json={"ip": "1.2.3.4"})
    assert resp.status_code == 422
