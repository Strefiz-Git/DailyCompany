# PROGRESS.md — DailyCompany

> Registo obrigatório de progresso. Mostra **em que fase estamos**, **o que já está feito** e **qual é o próximo passo**.
> Mantido pela skill `project-progress-tracker` a cada tarefa que muda o estado do projeto.
> Especificação → `PROJECT.md` · Visual → `DESIGN.md` · Ecrãs → `SCREENS.md` · Regras→ `AGENTS.md`.

---

## 1. Estado geral

| Campo | Valor |
|---|---|
| Fase atual | **0 — Fundações** (em curso) |
| Build | Não há ainda projeto Android criado (sem `app/`, sem Gradle) |
| Prioridade imediata | Criar o scaffold no Android Studio e montar os 5 ecrãs vazios navegáveis + tema claro/escuro + strings PT-PT |
| Branch ativa | `main` |

## 2. Fases do roadmap

Critério de conclusão de cada fase: compila · testes passam · commit na branch própria · merge para `main`.

| Fase | Objetivo | Estado | Pronto quando |
|---|---|---|---|
| **0 — Fundações** | Projeto, Gradle + version catalog, Hilt, tema e design tokens, navegação, Room vazio, strings PT-PT, `.gitignore` | 🔶 em curso | App corre, 5 ecrãs vazios navegáveis, claro/escuro a funcionar |
| **1 — Tarefas** | CRUD, Hoje básico, adiar, undo, estados vazios | ⚪ não iniciada | Consigo gerir um dia inteiro só com tarefas |
| **2 — Eventos** | Eventos + regras de repetição + calculadora de ocorrências testada + lista e calendário | ⚪ não iniciada | Aniversário anual e consulta a 3 meses funcionam |
| **3 — Lembretes e alarmes** | AlarmManager, notificações com ações, permissões, boot receiver | ⚪ não iniciada | Alarme toca; sobrevive a reinício |
| **4 — Guarda-roupa** | Fotos (picker/câmara/EXIF/thumbnails), peça completa, grelha, filtros, pesquisa, categorias editáveis | ⚪ não iniciada | 30 peças catalogadas sem lentidão |
| **5 — Outfits** | Construtor, gerador aleatório com cadeados, guardar com nome, usar hoje, histórico | ⚪ não iniciada | Botão de dados dá outfits plausíveis e guardáveis |
| **6 — Guia do dia** | Hoje completo (tarefas + eventos + lembretes + outfit), notificação de resumo | ⚪ não iniciada | Um único ecrã responde às duas perguntas |
| **7 — Polimento** | Acessibilidade, animações, desempenho, testes de UI, revisão de textos | ⚪ não iniciada | Checklists de `DESIGN.md` todas verdes |
| **8 — Backup e release** | Exportar/importar, assinatura, R8, AAB | ⚪ não iniciada | Restauro num telemóvel limpo fica idêntico |
| **Extras** | Meteorologia real, estatísticas, widget, inglês, arquivo por estação | ⚪ fora de âmbito | Só depois do essencial estável |

## 3. O que já está feito

**Documentação (pronta)**
- `PROJECT.md` — visão, funcionalidades, regras de negócio R1–R13, modelo de dados, arquitetura, roadmap.
- `DESIGN.md` — tokens de cor/tipografia/espaço, componentes, acessibilidade.
- `SCREENS.md` — mapa de navegação e especificação dos ecrãs.
- `AGENTS.md` — regras obrigatórias, stack, estrutura de pastas, fluxo de trabalho, skills.
- `README.md`, `LICENSE`, `.gitattributes`.

**Fundações pré-código (prontas)**
- `.opencode/skills/` — 17 skills de desenvolvimento (Compose, Gradle, Hilt, Room, UI/tema, i18n, fotos, garda-roupa/outfits, datas/eventos, alarmes, backup, testes, git, organização de pastas/código, progresso).
- `exemplos/` — implementações de referência reutilizáveis: `Theme.kt` + temas claro/escuro, `Language.kt` + `PtPtStrings`/`PtBrStrings`/`EnStrings`, `SettingsRepository.kt`, `MainActivity.kt`, `BackupManager.kt`/`Module`/`Screen`, snippet do `AndroidManifest.xml`, ícone de splash.
- `check_structure.py` (skill `app-code-organizer`) — diagnóstico automático de organização do código.
- `PROGRESS.md` — este ficheiro.

**Código da app (nada criado ainda)** — não existe `app/`, nem Gradle, nem código Kotlin na app.

## 4. Próximo passo (bloqueado no utilizador)

1. Criar o projeto Android no Android Studio (ver instruções na resposta do agente) — gera `app/`, `gradle/`, `gradlew`, version catalog.
2. O agente completa o scaffold: tokens do tema, línguas (PT-PT + pt-BR + EN a partir de `exemplos/`), navegação, 5 ecrãs em branco, Room vazio, Hilt, strings PT-PT, `.gitignore` Android.
3. Verificação: `./gradlew :app:assembleDebug` + commit na branch (ex. `feat/fase-0-fundacoes`).

## 5. Decisões tomadas recentemente

| Data | Decisão |
|---|---|
| 2026-09-20 | Criado `PROGRESS.md` e skill `project-progress-tracker` para registo obrigatório de progresso. |
| 2026-09-20 | Skills `project-folder-organizer` e `app-code-organizer` adicionadas; `check_structure.py` movido para a skill. |
| 2026-09-20 | Criação do `AGENTS.md` com a regra de carregar `kotlin-compose-android` em qualquer tarefa de código. |

## 6. Registo cronológico

| Data | O que mudou |
|---|---|
| 2026-09-20 | Fase 0 iniciada: documentação completa, skills prontas, `exemplos/` criado. Sem código Android ainda. |

---

**Regra:** nunca terminar uma tarefa sem atualizar as secções 3, 5 ou 6 deste ficheiro se o estado do projeto mudou.