# PROGRESS.md — DailyCompany

> Registo obrigatório de progresso. Mostra **em que fase estamos**, **o que já está feito** e **qual é o próximo passo**.
> Mantido pela skill `project-progress-tracker` a cada tarefa que muda o estado do projeto.
> Especificação → `PROJECT.md` · Visual → `DESIGN.md` · Ecrãs → `SCREENS.md` · Regras→ `AGENTS.md`.

---

## 1. Estado geral

| Campo | Valor |
|---|---|
| Fase atual | **2 — Eventos** (em curso 🔶 — Passo 1: domínio + calculadora + testes) |
| Build | `:app:assembleDebug` ✓ · `:app:testDebugUnitTest` ✓ · `:app:lintDebug` ✓ (com `JAVA_HOME` = `C:\Program Files\Java\jdk-21`; o JBR desta máquina é Java 25, não suportado pelo Gradle 8.13) — **stack atualizado**: Compose BOM 2026.04.01 (ui/foundation/animation 1.11.0, material3 1.4.0, icons 1.7.8), Kotlin 2.3.21, KSP 2.3.12, Room 2.8.4, Hilt 2.58 |
| Prioridade imediata | Passo 2 da Fase 2 (Eventos) — Room v3→v4 (`EventEntity`/`EventExceptionEntity`/DAO/converters + migração + schema `4.json`), repositório+DI, VMs, lista/edição; depois revalidar e seguir roadmap. **Fase 1: ainda falta validar em dispositivo + merge (aguarda OK)** |
| Branch ativa | `feat/events` |

## 2. Fases do roadmap

Critério de conclusão de cada fase: compila · testes passam · commit na branch própria · merge para `main`.

| Fase | Objetivo | Estado | Pronto quando |
|---|---|---|---|
| **0 — Fundações** | Projeto, Gradle + version catalog, Hilt, tema e design tokens, navegação, Room vazio, strings PT-PT, `.gitignore` | ✅ concluída | App corre, 5 ecrãs vazios navegáveis, claro/escuro a funcionar |
| **1 — Tarefas** | CRUD, Hoje básico, adiar, undo, estados vazios | 🔶 em curso | Consigo gerir um dia inteiro só com tarefas |
| **2 — Eventos** | Eventos + regras de repetição + calculadora de ocorrências testada + lista e calendário | 🔶 em curso — **Passo 1 (domínio + testes) concluído** | Aniversário anual e consulta a 3 meses funcionam |
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

**Fase 1 — Tarefas (em curso 🔶, código + testes concluídos, falta validar em dispositivo)**
- `feature/tasks/` — `TasksRoute.kt` + `TasksViewModel.kt` com separadores Hoje/Próximas/Sem data/Concluídas; snackbar e `contentDescription` via `AppStrings` (5 chaves novas ×3 línguas) e snackbar do ViewModel como enum `TaskSnackbar` (a UI resolve o texto).
- Camada Room: `TaskEntity` (`tasks`, colunas `due_date`/`created_at`/`done_at` + campo `doneAt`), `TaskDao` (queries alinhadas ao schema, sem duplicados), `TaskConverters`, `AppDatabase` v1 (`exportSchema=true`, schema `1.json` regenerado — v1 nunca foi instalado, sem migração), `TaskPriorityEntity`.
- `RoomTaskRepository` (mapeia `doneAt` nos dois sentidos; usa `AppClock` injetado, sem `System.currentTimeMillis()`).
- DI consolidado: `AppDiModule` único (Room, `AppClock`, `Dispatchers.IO.limitedParallelism(2)`) + `RepositoryModule` (`@Binds` `TaskRepository`); `AppModule` removido (duplicava `AppClock`).
- Editor de tarefas: `TaskEditRoute.kt` + `TaskEditViewModel.kt` (título, nota, data/hora com pickers, prioridade, guardar, apagar com undo) e rota `task_edit` no `DailyCompanyApp`.
- Testes unitários (17 testes verdes): `TasksViewModelTest` (quick add, título vazio ignorado, toggle done + undo, adiar + undo, separadores), `TaskEditViewModelTest`, `TaskGroupsTest` (agrupamento Hoje/Próximas/Sem data/Concluídas), `StringsCompletenessTest` (as 3 línguas completas, sem TODO/placeholders) + `MainDispatcherRule`/`FakeTaskRepository`/`FixedClock`.
- Lint corrigido: `Spacer(Modifier.weight(0f))` → `Spacer(Modifier.width(Spacing.xs))` em `TaskEditRoute` (`:app:lintDebug` verde).
- Limpeza feita: removidos os 29 ficheiros de debug (`.log`/`.txt`) commitados por engano; `*.log` no `.gitignore`.
- **Ícone da tarefa (novo, pedido do utilizador):** `TaskIcon` (sealed `Material`/`Emoji`) no domínio com serialização `m:nome`/`e:emoji` (round-trip testado em `TaskIconTest`), campo `icon` em `Task`/`TaskEntity` (coluna `icon` na BD, migração Room **v1→v2**, schema `2.json` gerado), mapper no `RoomTaskRepository`.
- Seletor de ícone no editor (`IconPicker`): **duas abas** — Ícones Material (14: casa, compras, refeição, ginásio, estudo, trabalho, corrida, saúde, favorito, estrela, calendário, email, telefone, check) e Emojis (16) — em grelha `FlowRow` (alvo 48dp), com opção "Sem ícone"; badge do ícone/emoji na `TaskRow` (decorativo, `contentDescription = null`). Catálogo + `materialIconVector`/`TaskIconBadge` em `feature/tasks/TaskIcons.kt`.
- Quick-add barra: **correção de layout** — estava composta *fora* do `Scaffold` como irmã sem container, pelo que era sobreposta **no topo do ecrã** (tapava as tabs) e os `navigationBarsPadding`/`imePadding` inflavam-lhe a altura; passou para `bottomBar` do Scaffold (fixa no fundo, com o espaço reservado ao conteúdo). Padding vertical mantido em `Spacing.xs` (antes `sm`).
- Quick-add barra (2.ª correção, feedback de inspeção): removido o `navigationBarsPadding()` interno — a barra está acima da `NavigationBar` da app, que já cobre o inset de navegação do sistema; o padding manual criava um vão vazio por baixo do campo (ficava encostado ao topo, com espaço antes da barra inferior). Mantido o `imePadding()` para subir com o teclado.
- Feedback de inspeção (3.ª ronda): (a) **topbar com faixa vazia em cima do título/⋮** — edge-to-edge + `Scaffold` externo já consomem a status bar (`innerPadding`), mas as `TopAppBar` internas reaplicavam `statusBars` por omissão; fix `windowInsets = WindowInsets(0.dp)` em `TopAppBarWithSettings` (5 ecrãs), `TaskEditRoute` e `SettingsRoute`. (b) **quick-add com altura a mais/falta de respiro** — padding vertical `Spacing.xs` → `Spacing.sm`, conteúdo mantém-se centrado na horizontal e vertical.
- Strings novas (×3 línguas): `tasksFieldIcon`, `tasksIconNone`, `tasksIconTabIcons`, `tasksIconTabEmojis`.
- Testes: 22 verdes (5 novos: 4 de `TaskIconTest` + 1 de `onIconChange` no editor).

## 4. Próximo passo (Fase 1 — Tarefas)

1. Testar a app em dispositivo: criar tarefas (quick add), concluir/reabrir com undo na snackbar, adiar para amanhã, editor (data/hora/prioridade/**ícone**), separadores, **ícone/emoji na lista**, **padding/altura da quick-add bar**, **topbar sem faixa vazia por cima do título e do ⋮** e tema claro/escuro. Nota: com a migração v1→v2, uma instalação existente tem de abrir sem perder dados.
2. **Validar visual pós-upgrade do stack** (branch `chore/upgrade-compose-stack`, ainda por fazer em dispositivo): o material3 1.4 mudou cores por omissão — `PrimaryTabRow` (indicador/container primário em vez do `TabRow` neutro antigo) e `NavigationBarItem` (label ativo agora `secondary` em vez de `onSurface`); os ícones `AutoMirrored` devem pintar igual. Se o aspeto não agradar, ajustar tokens no tema.
3. Verificação já feita: `:app:assembleDebug` + `:app:testDebugUnitTest` (22 testes) + `:app:lintDebug` verdes (com o novo stack).
4. Push das branches `feat/tasks-crud`, `feat/task-icons` e `chore/upgrade-compose-stack` para `origin` (aguardam OK do utilizador) e, no fim da fase, merge para `main`.

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
| 2026-09-22 | Adicionado workflow GitHub Actions `opencode.yml` (reage a `/opencode` ou `/oc` em comentários de issues e PRs) — dev tooling, não afeta o offline-first da app. Commit automático no fim de cada tarefa (sem pedir autorização) explícito no `AGENTS.md` regra 3 e na skill `git-commit-workflow`. |
| 2026-09-22 | Build da Fase 1 desbloqueado: `TaskEntity` alinhado ao DAO (`@ColumnInfo` `due_date`/`created_at`/`done_at` + campo `doneAt`); schema v1 regenerado (nunca instalado ⇒ sem migração); `RoomTaskRepository` com `Clock` injetado. |
| 2026-09-22 | DI consolidado: `AppModule` apagado (duplicava `AppClock` com `AppDiModule`), providers unidos num só `AppDiModule`; novo `RepositoryModule` com `@Binds` para `TaskRepository`. |
| 2026-09-22 | Textos visíveis das tarefas no lugar certo: snackbar como enum `TaskSnackbar` (UI resolve via `AppStrings`) e `contentDescription`/labels sem strings fixas (5 chaves novas ×3 línguas). |
| 2026-09-22 | Workflow GitHub passou a usar provider gratuito Gemini (`GOOGLE_API_KEY` + model `google/gemini-2.5-flash`) porque o utilizador não tem IA paga. Secret `GOOGLE_API_KEY` adicionado nas Actions settings do repo. |
| 2026-09-22 | GitHub App **OpenCode Agent** já se encontrava instalada e configurada para *All repositories* (cobre o `DailyCompany`). `opencode.yml` passou a `use_github_token: false` (token da app via OIDC) para os commits/PRs do bot aparecerem como `opencode-agent`, em vez de `GITHUB_TOKEN`. |
| 2026-09-22 | Ícone de tarefa pedido pelo utilizador: tipo `TaskIcon` (Material/Emoji) com serialização estável (`m:`/`e:`) em vez de guardar enum/UI na BD, e migração Room v1→v2 (`ALTER TABLE`) em vez de destruir dados. |

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
| 2026-09-22 | Workflow GitHub Actions `opencode.yml` criado (manual, porque esta versão do CLI não tem `opencode github install`) + skill `git-commit-workflow` e AGENTS.md regra 3 ajustados: commits no fim de cada tarefa **sem pedir autorização**; só merge/push/tag, apagar branches e operações destrutivas exigem OK explícito. |
| 2026-09-22 | Limpeza: removidos os 29 ficheiros de debug (`.log`/`.txt`) commitados por engano (estado preservado em `PROGRESS.md` e no histórico git); `*.log` adicionado ao `.gitignore`. |
| 2026-09-22 | Build da Fase 1 desbloqueado: `assembleDebug` + `testDebugUnitTest` + `lintDebug` verdes. Corrigidos `TaskEntity`/`TaskDao` (colunas + duplicado), `RoomTaskRepository` (`doneAt`, `Clock` injetado), `TasksViewModel`/`TasksRoute` (imports, visibilidade, âmbitos), i18n da snackbar/a11y, e DI consolidado. Nota: o JBR do Android Studio desta máquina é Java 25 — o build usa `C:\Program Files\Java\jdk-21`. |
| 2026-09-22 | Secret `GOOGLE_API_KEY` adicionado no GitHub e `opencode.yml` atualizado para Gemini (grátis) — pronto a usar com `/opencode` em issues/PRs. |
| 2026-09-22 | Verificada a instalação da GitHub App OpenCode Agent (*All repositories* — o repo fica coberto); `opencode.yml` com `use_github_token: false` para os commits/PRs do bot saírem como `opencode-agent` via token OIDC da app. |
| 2026-09-22 | Fase 1 concluída em código, commitada e sincronizada em `feat/tasks-crud` (commit `6c5a406` do bot — mensagem só "." por ser commit automático; trabalho completo da fase): CRUD de tarefas com editor (data/hora/prioridade), undo em progredir/adiar/apagar, separadores, testes unitários (17, verdes: Tasks/TaskEdit/groupTasks/completude das 3 línguas). `assembleDebug` + `lintDebug` + `testDebugUnitTest` verdes após corrigir `weight(0f)` num `Spacer` no editor. Falta: teste em dispositivo + OK para push/merge. |
| 2026-09-22 | Branch `feat/task-icons` criada (a partir de `feat/tasks-crud`): ícone de tarefa (Ícones Material + Emojis, duas abas no editor, badge na lista, migração Room v1→v2 com `ALTER TABLE tasks ADD COLUMN icon`, `TaskIcon` com round-trip testado) + fix do padding vertical da quick-add barra. 22 testes, `assembleDebug` + `lintDebug` verdes. Falta: teste em dispositivo + OK para push/merge. |
| 2026-09-22 | Feedback de inspeção na aba Tarefas: (1) quick-add barra com vão vazio por baixo do campo — removido o `navigationBarsPadding()` interno (a `NavigationBar` da app já cobre o inset do sistema); `assembleDebug` + `testDebugUnitTest` (22) + `lintDebug` verdes. (2) Aviso do Android Studio: `compose.ui:ui` resolvido para **1.9.2** por elevação transitiva do `lifecycle` 2.10.0 (pede Compose 1.9.x), acima do BOM 2024.09.00 (1.7.0) — sugestão de upgrade para 1.11.0; decisão pendente do utilizador (mudança de build grande). |
| 2026-09-22 | Upgrade do stack (aprovado pelo utilizador, branch `chore/upgrade-compose-stack`): **Compose BOM 2026.04.01** (ui/foundation/animation/material3 1.11.0/1.4.0 — resolve o aviso do Studio, `ui` deixa de ser 1.9.2 transitiva), **Kotlin 2.3.21** + **KSP 2.3.12**, **Room 2.8.4** (KSP2-safe), **Hilt 2.58** (última linha compatível com AGP 8.x e com o fix de metadata Kotlin 2.3; a 2.59+ exige AGP 9). Migrações de código exigidas: `kotlinOptions` → DSL `compilerOptions` (erro no Kotlin 2.3), `TabRow` → `PrimaryTabRow` (deprecated no material3 1.4), `Icons.Outlined.DirectionsRun` → variante `AutoMirrored`, `!!` desnecessário removido, `@param:ApplicationContext` (KT-73255). `assembleDebug` + `testDebugUnitTest` (22) + `lintDebug` verdes sem avisos. Falta: validar visual em dispositivo. |
| 2026-09-22 | Feedback de inspeção (3.ª ronda, branch `chore/upgrade-compose-stack`): **topbar** com faixa vazia em cima do título/⋮ (edge-to-edge: `Scaffold` externo já consome a status bar; `TopAppBar` internas reaplicavam `statusBars` — fix `windowInsets = WindowInsets(0.dp)` em `TopAppBarWithSettings`, `TaskEditRoute` e `SettingsRoute`) e **quick-add** com altura aumentada (`Spacing.xs` → `sm`, conteúdo centrado). `assembleDebug` + `lintDebug` verdes. Falta revalidar em dispositivo. |
| 2026-09-22 | Fase 1 — **repetição de tarefas** (branch `feat/task-recurrence`): `TaskRepeat` de domínio (NONE/DAILY/WEEKLY/MONTHLY/YEARLY) com `nextDueDate()` puro modelado (clamp dia 31 → fim do mês e 29-fev → 28-fev; roll-forward a partir da base original, corrigia o bug que encadeava candidatos e devolvia Mar 28 em vez de Mar 31), concluir repetitiva avança para a próxima ocorrência com undo, seletor no editor (FilterChips), badge de repetição na lista, **emoji livre** (campo de texto no editor — substitui a grelha fixa) e migração Room v2→v3 (`ALTER TABLE tasks ADD COLUMN repeat`) com schema `3.json` versionado. Strings nas 3 línguas. `assembleDebug` + 31 testes + `lintDebug` verdes. Falta: validar em dispositivo.
| 2026-09-23 | Fase 2 — **Passo 1 (Eventos) concluído** (branch `feat/events`, commit `7c0ac59`): `Event.kt` (domínio puro `java.time`: `RecurrenceRule` NONE/DAILY/WEEKLY/MONTHLY/YEARLY com `interval`/`weekDays`/`monthPosition`, `RecurrenceEnd` Never/OnDate/AfterCount, `MonthPosition`), `OccurrenceCalculator.kt` **determinístico e ancorado a `event.date`** (clamp mensal dia 31→fim do mês e 29-fev→28-fev antes de `atDay`; "mesma posição" mensal omite se não existir; anual 29-fev→28-fev; janelas `from..to`; fins Never/OnDate/AfterCount com corte global; **R6 inviolável — ocorrências nunca são gravadas, só calculadas**) e `EventOccurrenceCalculatorTest.kt` (15 testes, verdes). `testDebugUnitTest` (15/15) + `assembleDebug` verdes. Falta: validar em dispositivo; o Passo 2 (Room v3→v4 + repositórios + UI) é o próximo; merge/push aguarda OK. |
| 2026-09-23 | Fase 2 — **Passo 1 (Eventos) concluído** (branch `feat/events`, commit `7c0ac59`): domínio puro `Event.kt` (`RecurrenceRule` NONE/DAILY/WEEKLY/MONTHLY/YEARLY + `interval`/`weekDays`/`monthPosition`; `RecurrenceEnd` Never/OnDate/AfterCount; `MonthPosition`), `OccurrenceCalculator.kt` determinístico ancorado a `event.date` (clamp dia 31 → fim do mês e 29-fev → 28-fev **antes** de `atDay`; mensal "mesma posição" omite se não existir; anual aniversário; janelas `from..to`; fins Never/OnDate/AfterCount com corte global; **R6 inviolável — nunca grava ocorrências**) e `EventOccurrenceCalculatorTest.kt` (15 testes verdes). `testDebugUnitTest` (15/15) + `assembleDebug` verdes. Falta: Passo 2 (Room v3→v4) + validar em dispositivo; merge/push aguarda OK. |

---

**Regra:** nunca terminar uma tarefa sem atualizar as secções 3, 5 ou 6 deste ficheiro se o estado do projeto mudou.
