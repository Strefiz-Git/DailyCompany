---
name: project-progress-tracker
description: MANDATORY progress tracking for this repository. A cada tarefa que muda o estado do projeto (código, docs, builds, decisões), atualiza PROGRESS.md na raiz: fase atual, o que mudou, o próximo passo. Carrega (lê) PROGRESS.md no início de tarefas grandes para saber onde estamos e consulta PROJECT.md roadmap. Works in PT-PT: the document and updates are written in European Portuguese, identifiers/commits stay in English.
license: MIT
compatibility: opencode
metadata:
  scope: all-tasks
  mandatory: "true"
---

# Progress tracker do DailyCompany

Mantém o ficheiro **`PROGRESS.md`** (raiz do repositório) como fonte da verdade sobre **em que fase estamos**, **o que já está feito** e **qual o próximo passo**. Sem registo, a tarefa não está feita.

## 1. Antes de começar uma tarefa grande ou ambígua

1. Ler `PROGRESS.md` (secções 1–4) para saber o estado atual e a fase.
2. Ler a fase correspondente do roadmap em `PROJECT.md` §12 (critério "Pronto quando").
3. Confirmar que a tarefa não está já feita nem bloqueada.

## 2. Durante a tarefa

- Guardar marcos intermédios (build a compilar, commits, decisões) para registar no fim.
- Se a decisão mudar o âmbito ou o roadmap, anotar para propor ao utilizador (nunca alterar `PROJECT.md` por iniciativa própria).
- Changes that belong to a phase: keep the phase status as "em curso" (🔶) until the "Pronto quando" is verifiably met — compile + tests + merge. Only then flip to ✅ concluída.

## 3. No fim de cada tarefa (sempre)

Atualizar as secções de `PROGRESS.md` que mudaram:

| Secção | Atualizar quando… |
|---|---|
| §1 Estado geral | fase atual, build, prioridade imediata mudarem |
| §2 Fases do roadmap | uma fase passar a em curso 🔶 ou concluída ✅ |
| §3 O que já está feito | algo novo esteja feito (código, docs, skills, scripts) |
| §4 Próximo passo | o bloqueio/prioridade mudar (atualizar o texto existente, não acumular) |
| §5 Decisões | se tomou/seguir uma decisão relevante |
| §6 Registo cronológico | adicionar uma linha com a data e o que mudou |

Regras de escrita:
- **PT-PT**, conciso, factual. Sem promessas nem funcionalidades que não existem.
- Dates `YYYY-MM-DD` (hoje é a data real do sistema).
- Nunca marcar uma fase como concluída com base na intenção: só depois de **compilar + testes + merge**.
- Não apagar histórico: manter as linhas antigas de §5/§6 e acrescentar as novas.
- Se a tarefa não mudou nada do estado (ex.: só uma pergunta), não é preciso atualizar.

## 4. Exemplos

Fase concluída (§2):

```
| **0 — Fundações** | … | ✅ concluída | App corre, 5 ecrãs vazios navegáveis, claro/escuro a funcionar |
```

Nova linha de registo (§6):

```
| 2026-09-20 | Fase 0 concluída: projeto criado no Android Studio, tema e navegação a compilar (`assembleDebug` OK). |
```

## 5. Relatório final

No relatório de fim de tarefa (git-commit-workflow §10), acrescentar: **progresso atualizado em `PROGRESS.md`** e a fase em que ficámos.