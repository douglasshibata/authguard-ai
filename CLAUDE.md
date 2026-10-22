# AuthGuard AI - CLAUDE.md

Guia de comandos e diretrizes para o projeto AuthGuard AI.

## Comandos de Build e Execução

### Backend (Java 21 / Spring Boot 3.5)
```bash
cd authguard-ai/backend
mvn spring-boot:run
# audit: mvn org.owasp:dependency-check-maven:check
```

### AI Engine (Python 3.12 / FastAPI)
```bash
cd authguard-ai/ai-engine
python -m venv .venv
source .venv/bin/activate          # Windows: .venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
# tests: pip install -r requirements-dev.txt && pytest
```

### Frontend (Angular PWA)
```bash
# Comandos padrão Angular (requer Node.js e Angular CLI)
npm install
ng serve
```

## Diretrizes de Desenvolvimento

- **Segurança Zero-Trust:** Toda requisição deve ser validada tanto pelo token TOTP quanto pelo Risk Engine.
- **TOTP:** Segue a RFC 6238. Os segredos devem ser armazenados de forma criptografada.
- **Análise de Risco:** O `ai-engine` avalia anomalias comportamentais. Um `risk_score > 80` bloqueia o acesso.
- **Frontend:** Desenvolvido como PWA para suporte offline.
