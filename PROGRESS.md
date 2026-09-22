# PROGRESS.md — DailyCompany

> Registo obrigatório de progresso. Mostra **em que fase estamos**, **o que já está feito** e **qual é o próximo passo**.
> Mantido pela skill `project-progress-tracker` a cada tarefa que muda o estado do projeto.
> Especificação → `PROJECT.md` · Visual → `DESIGN.md` · Ecrãs → `SCREENS.md` · Regras→ `AGENTS.md`.

---

## 1. Estado geral

| Campo | Valor |
|---|---|
| Fase atual | **1 — Tarefas** (em curso 🔶) |
| Build | `:app:assembleDebug` ✗ (falha no Room KSP — colunas do DAO vs schema; causa identificada) · `:app:testDebugUnitTest` ✓ · `:app:lintDebug` ✓ |
| Prioridade imediata | Corrigir `TaskEntity`/`TaskDao` (alinhar colunas) e voltar a compilar |
| Branch ativa | `feat/fase-1-tarefas` |

## 2. Fases do roadmap

Critério de conclusão de cada fase: compila · testes passam · commit na branch própria · merge para `main`.

| Fase | Objetivo | Estado | Pronto quando |
|---|---|---|---|
| **0 — Fundações** | Projeto, Gradle + version catalog, Hilt, tema e design tokens, navegação, Room vazio, strings PT-PT, `.gitignore` | ✅ concluída | App corre, 5 ecrãs vazios navegáveis, claro/escuro a funcionar |
| **1 — Tarefas** | CRUD, Hoje básico, adiar, undo, estados vazios | 🔶 em curso | Consigo gerir um dia inteiro só com tarefas |
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

**Código da app (Fase 0 em curso)**
- Scaffold Android criado no Android Studio e integrado: repositório movido para a raiz (`gradle/`, `build.gradle.kts`, `settings.gradle.kts`, `gradlew`, `app/`).
- Package renomeado `com.example.dailycompany` → `com.dailycompany`; `minSdk` 26.
- Version catalog atualizado: Hilt + KSP, DataStore Preferences, Navigation Compose, `lifecycle-viewmodel-compose`, `core-splashscreen`, `material-icons-extended`.
- Tema: `Color.kt` (tokens de DESIGN.md, claro/escuro), `Dimens.kt` (`Spacing`, `AppShapes`), `Type.kt` (pesos/tamanhos de DESIGN.md, fontes do sistema por agora), `Theme.kt` (`DailyCompanyTheme` com `ThemeMode`, locale, `AppStrings`). `LocalContext` não é substituído (partia o `hiltViewModel()`); o locale chega aos composables por `LocalConfiguration` + `LocalAppLocale`.
- Línguas: `AppStrings` + `PtPtStrings`/`PtBrStrings`/`EnStrings` (completas nas três línguas).
- `SettingsRepository` + `StringsProvider` (DataStore), `AppSettingsViewModel` com splash até a 1.ª leitura.
- `DailyApp` (`@HiltAndroidApp`), splash (temas XML claro/escuro + `ic_splash_icon`).
- Navegação: `DailyCompanyApp` com `NavigationBar` de 5 destinos + `NavHost`; 5 ecrãs vazios (`today`, `tasks`, `events`, `wardrobe`, `outfits`) com `EmptyState`.
- Componentes partilhados: `TopAppBarWithSettings` (menu ⋮ → Definições) e `SectionHeader`.
- Ecrã **Definições**: `SettingsViewModel` + `SettingsRoute`/`SettingsScreen` (stateless, com preview claro/escuro) — tema Claro/Escuro/Sistema, cor dinâmica e língua (Seguir o sistema · PT-PT · PT-BR · EN), aplicadas de imediato e persistidas no DataStore.
- Navegação: rota `settings` em full screen (bottom bar escondida, back na `TopAppBar`); menu ⋮ ligado nos 5 destinos.
- `.gitignore` da raiz (gradle, `.kotlin/`, keys, APKs).

**Nota pendente (fase visual):** empacotar as fontes Outfit/Inter em `res/font/` — usam-se as do sistema.

**Fase 1 — Tarefas (em curso 🔶)**
- `feature/tasks/` — `TasksRoute.kt` + `TasksViewModel.kt` com separadores Hoje/Próximas/Sem data/Concluídas.
- Camada Room: `TaskEntity` (`tasks`), `TaskDao` (queries + `markDone`/`markNotDone`/`setDueDate`), `TaskConverters` (`LocalDate`/`LocalTime`/`TaskPriorityEntity`), `AppDatabase` v1 (`exportSchema=true`, schema `1.json` exportado), `TaskPriorityEntity` (`NONE`/`MEDIUM`/`HIGH`).
- `RoomTaskRepository` (concluir/adiar), `AppDiModule` (dispatchers: `Dispatchers.IO.asLimitedParallelism(2)` + qualifiers).
- **Bloqueio do build:** Room KSP falha — as `@Query` do `TaskDao` usam colunas snake_case (`created_at`, `done_at`, `due_date`) mas o `TaskEntity` sem `@ColumnInfo` gera colunas camelCase (schema v1). Próximo passo: alinhar.
- **Pendência de limpeza:** os commits "." gravaram ~36 ficheiros de debug (logs/txt) no repo — remover com aprovação do utilizador.

## 4. Próximo passo (Fase 1 — Tarefas)

1. **Desbloquear o build:** alinhar `TaskEntity.kt` (adicionar `@ColumnInfo` snake_case e o campo `doneAt`) ou as queries do `TaskDao` ao schema v1 (camelCase); `RoomTaskRepository.setDone` passa a chamar `markDone(id)`/`markNotDone(id)`. Rebuild `:app:assembleDebug`.
2. Terminar Fase 1: CRUD completo, adiar, undo (Snackbar ≥5 s), estados vazios; ecrã `tasks` conforme `SCREENS.md` §3.
3. Verificação: `:app:assembleDebug` + `:app:testDebugUnitTest` + teste em dispositivo.
4. Limpeza do repo (pendente de aprovação): remover os ficheiros de debug (`.log`/`.txt` da raiz) e os commits "." desnecessários.

## 5. Decisões tomadas recentemente

| Data | Decisão |
|---|---|
| 2026-09-20 | Scaffold Android integrado na raiz; package `com.dailycompany`; `minSdk` 26. |
| 2026-09-20 | Stack base aprovada pelo utilizador: Hilt+KSP, DataStore, Navigation, splash, `material-icons-extended`. |
| 2026-09-20 | Fontes Outfit/Inter adiadas para a fase visual; usa-se a do sistema com os mesmos pesos. |
| 2026-09-20 | Criado `PROGRESS.md` e skill `project-progress-tracker` para registo obrigatório de progresso. |
| 2026-09-20 | Builds no PC exigem `JAVA_HOME` a apontar para um JDK 21 (JBR do Android Studio ou `jdk-21`); o JDK 25 da PATH parte o Kotlin script compiler do Gradle 8.13. |
| 2026-09-20 | `DailyCompanyTheme` deixou de substituir `LocalContext` por `createConfigurationContext(...)`: causava `IllegalStateException: Expected an activity context ...` no `hiltViewModel()` do ecrã Definições. O locale passa por `LocalConfiguration` + `LocalAppLocale`. |
| 2026-09-20 | Skills `project-folder-organizer` e `app-code-organizer` adicionadas; `check_structure.py` movido para a skill. |
| 2026-09-20 | Criação do `AGENTS.md` com a regra de carregar `kotlin-compose-android` em qualquer tarefa de código. |
| 2026-09-22 | Branch `feat/fase-1-tarefas` criada para a Fase 1 (Tarefas). Os commits "." gravaram ~36 ficheiros de debug (logs/txt) no repo por engano — pendente de limpeza com aprovação. |
| 2026-09-22 | Diagnóstico do build: Room KSP "no such column: created_at/done_at/due_date" — as `@Query` do `TaskDao` usam snake_case mas o `TaskEntity` (sem `@ColumnInfo`) gera colunas camelCase (schema v1 confirmado). |
| 2026-09-22 | Otimização anti-conflito dos docs/skills (decisão desta tarefa): texto de ecrãs só via `AppStrings` (removeu-se a contradição `strings.xml` do AGENTS/PROJECT/skills), prefixos de branch padronizados (`feat/`/`exp/`), criadas as skills em falta `android-hilt-di`, `android-compose-ui-design`, `android-images-media`, ordem de autoridade clarificada no `AGENTS.md`, `app-code-organizer` alinhado à estrutura real, `android-wardrobe-outfits` reescrito com R1–R5/R13. |

## 6. Registo cronológico

| Data | O que mudou |
|---|---|
| 2026-09-20 | Fase 0 validada em dispositivo pelo utilizador (splash, tema claro/escuro, 5 ecrãs navegáveis). Fase 0 concluída; falta commit/merge. |
| 2026-09-20 | Ecrã Definições (tema, cor dinâmica, língua) + `TopAppBarWithSettings` com ⋮ + `SectionHeader`; rota `settings` full screen a esconder a bottom bar, ⋮ ligado nos 5 destinos. Nomes das línguas movidos para `AppStrings`. Build + testes + lint verdes (com `JAVA_HOME` apontando a JDK 21 — o JDK 25 da PATH parte o script compiler do Gradle). |
| 2026-09-20 | Corrigido crash ao abrir Definições: o tema substituía `LocalContext` por um contexto sem Activity, partindo o `hiltViewModel()`. Removido o override; build + testes + lint verdes. Falta revalidar em dispositivo. |
| 2026-09-20 | Fase 0: scaffold movido para a raiz, package `com.dailycompany`, Hilt/DataStore/Navigation/splash adicionados, tema claro/escuro com tokens do DESIGN.md, 3 línguas completas, navegação com 5 ecrãs vazios. Build + testes + lint verdes. |
| 2026-09-20 | Fase 0 iniciada: documentação completa, skills prontas, `exemplos/` criado. Sem código Android ainda. |
| 2026-09-22 | Build da Fase 1 a falhar: Room KSP rejeita as queries do `TaskDao` (colunas snake_case vs schema camelCase). Estado completo documentado no `continuar.txt`. |
| 2026-09-22 | Otimização anti-conflito dos `.md`: removida a contradição `strings.xml` vs `AppStrings` (AGENTS.md §3.5, PROJECT.md §13, skills `kotlin-compose-android`, `android-daily-planner`, `android-alarms-notifications`, `android-recurring-events-dates`), prefixos de branch alinhados (`feat/`/`exp/` no `git-commit-workflow`), 3 skills em falta criadas, `app-code-organizer` alinhado ao `AGENTS.md` §2, `android-wardrobe-outfits` reescrito com o modelo real (R1–R5/R13). |

---

**Regra:** nunca terminar uma tarefa sem atualizar as secções 3, 5 ou 6 deste ficheiro se o estado do projeto mudou.