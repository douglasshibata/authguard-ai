# SKILLS.md - AuthGuard AI

Diretrizes para evolução e segurança do autenticador cognitivo.

## 🛡️ Auditoria de Segurança

1. **Criptografia de Seeds:** Verifique se os segredos TOTP (Seeds) nunca são expostos em texto claro em logs ou respostas de API.
2. **Entropia:** Garanta que a geração de novos segredos utiliza fontes de entropia seguras do sistema operacional.
3. **Rate Limiting:** Audite a implementação de bloqueio após tentativas falhas no backend Spring Boot.
4. **Proteção PWA:** Verifique a segurança do armazenamento local (IndexedDB) no frontend Angular.

## 📈 Melhorias Sugeridas

1. **Cognição:**
   - Expandir o `ai-engine` para analisar velocidade de digitação (keystroke dynamics).
   - Integrar geolocalização IP de maior precisão.
2. **Arquitetura:**
   - Implementar suporte a Hardware Security Modules (HSM) para armazenamento de chaves mestras.
   - Adicionar suporte a WebAuthn (FIDO2) como alternativa ao TOTP.
3. **UX/UI:**
   - Melhorar o fluxo de "setup" inicial com geração dinâmica de QR Code seguro.

## 🤖 Papel do Claude

- **Especialista em AppSec:** Realize revisões focadas na implementação do algoritmo TOTP e proteção contra ataques de repetição (replay attacks).
- **Cientista de Dados:** Sugira melhorias no modelo de detecção de anomalias do `ai-engine`.
- **Fullstack Dev:** Ajude na integração assíncrona entre o backend Spring Boot e o motor de risco Python.
