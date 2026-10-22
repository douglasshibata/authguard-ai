# AuthGuard AI: Zero-Trust Cognitive Authenticator

AuthGuard AI is a next-generation authentication system that combines RFC 6238 (TOTP) with real-time behavioral risk analysis to enforce a Zero-Trust security model.

## 🛡️ Core Concept
Standard 2FA isn't enough. AuthGuard AI analyzes *how* a user interacts with the system. If the behavioral risk score exceeds threshold (>80), access is blocked even if the TOTP code is correct.

## 🚀 Technologies

- **Backend:** Java 21, Spring Boot 3.4, Spring Security.
- **AI Engine:** Python 3.12, FastAPI, Pydantic 2.9.
- **Frontend:** Angular 19, PWA (Progressive Web App) for offline TOTP generation.
- **Security:** Zero-Trust Architecture, AES-256 encrypted storage.

## 📁 Project Structure

```
authguard-ai/
├── backend/            # Spring Boot TOTP & Validation Engine
├── ai-engine/          # FastAPI Risk Analysis Motor
├── frontend/           # Angular PWA Client
└── demo/               # Integration examples
```

## 💻 How to Run

### 1. Start Backend (TOTP Engine)
```bash
cd backend
./mvnw spring-boot:run
```

### 2. Start AI Engine (Risk Motor)
```bash
cd ai-engine
pip install -r requirements.txt
uvicorn main:app --port 8000
```

### 3. Start Frontend
```bash
cd frontend
npm install
ng serve
```

## 🔐 Zero-Trust Guidelines
- **Encryption:** Secrets (seeds) are never stored in plaintext and never logged.
- **Validation:** Always validate both the TOTP token AND the Risk Score.
- **Resilience:** The frontend stores keys securely in IndexedDB to allow offline code generation.

---

## 🛠️ Project Deep Dive & Technical Guide

### 🎯 Main Purpose
AI-powered zero-trust continuous authentication and security guard service.

### 🧰 Tecnologias Utilizadas (Tech Stack)
- **Primary Technologies:** FastAPI, Java 21, Maven, Python 3.12, Spring Boot 3.4.3
- **Deployment Strategy:** Container-ready with standard Docker/Docker Compose integrations where applicable.

### 🚀 Como Implantar e Executar (How to Deploy & Run)
#### Backend (Java / Spring Boot)
1. Navigate to the backend directory.
2. Compile and run: `./mvnw spring-boot:run` or `mvn spring-boot:run`.

#### Python Engine / FastAPI
1. Install dependencies: `pip install -r requirements.txt`.
2. Run the server/script: `uvicorn main:app --reload` or `python main.py`.

---
