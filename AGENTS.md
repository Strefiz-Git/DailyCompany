# AGENTS.md — DailyCompany

App Android (Kotlin + Jetpack Compose), offline-first, com interface em **pt-PT, pt-BR e inglês** e **tema claro/escuro**.
Este ficheiro é obrigatório. Lê-o antes de qualquer tarefa e segue-o sempre.

**Documentos do projeto (lê o que for relevante antes de trabalhar):**
- `PROJECT.md` — o que a app é, funcionalidades, regras de negócio, modelo de dados, roadmap.
- `DESIGN.md` — identidade visual, tokens de cor/tipografia/espaço, componentes, acessibilidade.
- `SCREENS.md` — mapa de navegação e especificação de cada ecrã.
- `.opencode/skills/*/SKILL.md` — como fazer cada tipo de tarefa.

Em caso de conflito, a ordem de autoridade é: pedido explícito do utilizador → `PROJECT.md` → `AGENTS.md` → `DESIGN.md`/`SCREENS.md` → skills.

---

## 1. Regras obrigatórias (sem exceções)

1. **Carrega sempre a skill `kotlin-compose-android`** antes de escrever ou alterar código.
2. **Carrega também as skills da área da tarefa** (tabela na secção 4). Duas áreas ⇒ duas skills.
3. **Git: nunca trabalhes sem `git-commit-workflow`.** Commits com descrição, branch para mudanças experimentais ou grandes. Nunca `git push --force` no `main`.
4. **Nenhum texto visível escrito à mão num ecrã.** Todos os textos vivem em `ui/theme/languages/` (`PtPtStrings`, `PtBrStrings`, `EnStrings`) através da interface `AppStrings`; nos composables usa-se `strings()`. Chave nova = as três línguas preenchidas. Skill `android-i18n-theme`.
5. **Código, identificadores, comentários, commits e branches em inglês.** As respostas ao utilizador e a língua de referência da UI são PT-PT.
6. **Nenhuma cor, tamanho de letra ou raio hardcoded num ecrã.** Só tokens de `ui/theme` (`DESIGN.md`). O tema tem de funcionar em claro e escuro.
7. **Não inventes versões de bibliotecas.** Tudo vem de `gradle/libs.versions.toml`.
8. **Compila antes de dizer que está feito:** `./gradlew :app:assembleDebug` (+ `:app:testDebugUnitTest` se mexeste em lógica). Reporta o resultado real; se não correste, diz.
9. **Não testaste em dispositivo? Diz.** Fotos, alarmes, notificações e visual precisam de teste manual.
10. **Não adiciones dependências, permissões ou serviços cloud sem perguntar.**
11. **Offline-first:** sem contas, sem servidores, sem analytics, sem envio de dados.
12. **Não inventes funcionalidades nem ecrãs** que não estejam em `PROJECT.md`/`SCREENS.md` — propõe primeiro.
13. **Respostas ao utilizador em PT-PT**, concisas, com blocos de código completos e prontos a copiar (não diffs parciais).
14. Tarefa ambígua ou grande ⇒ **plano em 3–8 passos antes de escrever código**.

---

## 2. Stack e arquitetura

- **Kotlin**, **Jetpack Compose**, **Material 3**, single-Activity + Navigation Compose.
- **MVVM + UDF:** o `ViewModel` expõe `StateFlow<UiState>`; a UI é stateless e recebe estado + lambdas. Só as rotas (`XxxRoute`) chamam `hiltViewModel()`.
- **Room** para dados, **DataStore (Preferences)** para definições.
- **Hilt** (DI), **WorkManager** (background), **AlarmManager** (alarmes exatos), **Coil** (imagens), **kotlinx.serialization** (backup em zip).
- **Línguas e tema** resolvidos em `ui/theme` e persistidos em DataStore (`SettingsRepository`): `AppLanguage` (pt-PT, pt-BR, en) e `ThemeMode` (claro, escuro, sistema).
- **java.time** com desugaring; `Clock` injetado — nada de `System.currentTimeMillis()` espalhado.
- Fotos em `filesDir/images/`; na BD guarda-se só o **nome do ficheiro**.
- Camadas: `ui/` → `domain/` (modelos + use cases puros) → `data/`. A UI nunca fala com DAOs.
- `minSdk 26`, `targetSdk` atual, Compose BOM, KSP (nunca kapt).

### Estrutura de pastas

```
app/src/main/java/com/dailycompany/
├── DailyApp.kt
├── MainActivity.kt
├── core/            # utils, extensions, Clock, dispatchers, Result
├── ui/
│   ├── theme/       # Color.kt, Type.kt, Dimens.kt, Theme.kt, Language.kt
│   │   └── languages/  # PtPtStrings.kt, PtBrStrings.kt, EnStrings.kt
│   ├── components/  # componentes partilhados (ver DESIGN.md §7)
│   └── navigation/  # NavHost, rotas, deep links
├── data/
│   ├── local/       # Room (entities, daos, converters, migrations), DataStore
│   ├── repository/
│   ├── image/       # ImageProcessor, ImageStorage
│   ├── settings/    # SettingsRepository (tema, língua), StringsProvider
│   └── backup/      # BackupManager (.zip), DTOs, BackupRepository
├── domain/
│   ├── model/
│   └── usecase/     # GenerateOutfitUseCase, NextOccurrenceUseCase, ScheduleRemindersUseCase
├── feature/
│   ├── today/ tasks/ events/ alarms/ wardrobe/ outfits/ settings/
└── di/
```

---

## 3. Fluxo de trabalho de cada tarefa

1. Ler o pedido + a secção relevante de `PROJECT.md`/`SCREENS.md` + as skills da área.
2. `git status`; mudança grande ou experimental ⇒ `git checkout -b feat/<nome>`.
3. Plano curto se não for trivial.
4. Implementar: domínio puro primeiro (com testes), depois dados, depois UI.
5. Strings para `strings.xml`; tokens do tema na UI; componentes reutilizados de `ui/components/`.
6. `./gradlew :app:assembleDebug` (+ `testDebugUnitTest`, + `lintDebug` se mexeste em UI/strings).
7. Commits pequenos e descritivos.
8. Resumo final: o que mudou · o que foi testado · o que falta validar em dispositivo.

---

## 4. Skills e quando as usar

Local: `.opencode/skills/<nome>/SKILL.md`

| Skill | Carrega quando… |
|---|---|
| `kotlin-compose-android` | **sempre** que há código |
| `app-code-organizer` | ficheiros/pacotes novos, refactors, mover código |
| `project-folder-organizer` | estrutura do repositório, docs, ficheiros de raiz |
| `git-commit-workflow` | **sempre** que há commits, branches, merges |
| `android-gradle-build` | Gradle, versões, plugins, R8, erros de build |
| `android-hilt-di` | DI, módulos, ViewModels, erros Dagger/Hilt |
| `android-data-offline` | Room, DAOs, migrações, DataStore, repositórios |
| `android-compose-ui-design` | ecrãs, componentes, tema, estados, animações, acessibilidade |
| `android-i18n-theme` | qualquer texto visível, línguas, datas, plurais, tema claro/escuro |
| `android-images-media` | fotos: picker, câmara, EXIF, thumbnails, Coil |
| `android-wardrobe-outfits` | roupas, categorias, tamanhos, outfits, botão aleatório |
| `android-daily-planner` | tarefas do dia, guia do dia, ecrã Hoje |
| `android-recurring-events-dates` | eventos, repetições, próximas ocorrências |
| `android-alarms-notifications` | alarmes exatos, canais, permissões, boot |
| `android-backup-restore` | exportar/importar **tudo**: dados de domínio + definições + fotos, num só ficheiro |
| `android-testing` | testes unitários, DAO, Compose UI |
| `android-debugging-adb` | logcat, crashes, ANRs, inspeção de BD |
| `android-maps-location` | localização (meteorologia por cidade/coordenadas) |
| `android-release-security` | assinatura, release, permissões, privacidade |

**Regra prática:** UI + texto ⇒ `android-compose-ui-design` + `android-i18n-theme`.
Funcionalidade com dados ⇒ `android-data-offline` + skill da funcionalidade + `android-testing`.

---

## 5. Git (resumo — detalhe em `git-commit-workflow`)

- Branches: `main` · `feat/<tema>` · `fix/<tema>` · `exp/<tema>` · `chore/<tema>` · `docs/<tema>`.
- Formato:

```
<tipo>(<escopo>): <resumo em inglês, imperativo, ≤72 chars>

- o que mudou e porquê
- decisões relevantes ou trade-offs
- o que ficou por fazer
```

- Tipos: `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `build`, `chore`.
- Um commit = uma ideia. Nada de "misc changes".
- Nunca commitar `local.properties`, keystores, `.env`, `build/`, `.idea/`.
- Mudanças grandes/experimentais só entram no `main` depois de compilar e testar.

---

## 6. Qualidade

- Sem `!!`, sem `GlobalScope`, sem `runBlocking` em produção.
- Nada de I/O na main thread; `Dispatchers.IO` injetado por qualifier.
- Erros: `Result`/`sealed interface`, mensagem humana na UI, nunca a exceção em bruto.
- Estados de ecrã: Loading · Content · Empty · Error(retry).
- Apagar ⇒ **Anular** (Snackbar, ≥5 s).
- Acessibilidade: alvos ≥48 dp, `contentDescription`, texto legível a 200%.
- **Testes obrigatórios:** gerador de outfits · próximas ocorrências de eventos · parsing/ordenação de tamanhos · agendamento de lembretes · round-trip **completo** do backup (dados + definições) · completude das três línguas.
- Regras de negócio R1–R10 de `PROJECT.md` §9 são invioláveis.

---

## 7. Privacidade

Tudo local. Sem contas, sem servidores, sem tracking. Permissões só as necessárias, pedidas no momento em que fazem sentido, com explicação em PT-PT. O backup é um ficheiro que o utilizador escolhe onde guardar.