# AGENTS.md

Este arquivo fornece instruções e contexto para agentes de IA que trabalham no projeto **AuthGuard AI**.

## Visão Geral do Projeto
Autenticador cognitivo Zero-Trust que combina TOTP (RFC 6238) com análise de risco comportamental em tempo real.

## Tecnologias Utilizadas
- **Backend:** Java (Spring Boot), TOTP Engine.
- **AI Engine:** Python (FastAPI), Risk Model.
- **Frontend:** Angular PWA (Progressive Web App).
- **Segurança:** Zero-Trust Architecture, RFC 6238.

## Estrutura de Pastas
- `backend/`: Geração e validação de tokens TOTP.
- `ai-engine/`: Motor de análise de risco cognitivo.
- `frontend/`: Cliente PWA para geração local de códigos.

## Comandos Principais
- Backend: `./mvnw spring-boot:run`
- AI Engine: `uvicorn main:app --port 8000`
- Frontend: `ng serve`

## Diretrizes de Desenvolvimento
- **Zero-Trust:** Valide tanto o token TOTP quanto o Risk Score (>80 bloqueia).
- **Segurança:** Segredos (Seeds) devem ser armazenados de forma criptografada e nunca expostos em logs.
- **Resiliência:** O frontend deve funcionar offline como PWA, armazenando chaves no IndexedDB de forma segura.
- **Proteção:** Implemente Rate Limiting agressivo e proteção contra replay attacks.
