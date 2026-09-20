---
name: app-code-organizer
description: Organiza os ficheiros de código DENTRO da app - packages, camadas, features, nomes de ficheiros, recursos (res/), e módulos Python/Flask. Usar SEMPRE que o utilizador pedir para organizar, arrumar, reestruturar ou refatorar a estrutura do código, quando houver ficheiros Kotlin/Java/Python soltos ou numa pasta gigante, ficheiros demasiado grandes, packages que não batem com as pastas, ou ao criar uma feature nova e decidir onde põe cada ficheiro. Cobre Android/Kotlin/Compose e backend Python. Para a raiz do repositório, docs e scripts usar project-folder-organizer. EN - organize source code, package structure, feature folders, clean architecture layout.
---

# Organizador do código da app

Objetivo: cada ficheiro de código tem uma casa óbvia, com nome previsível, e quem abre o projeto encontra qualquer coisa sem pesquisar. Esta skill trata do **interior** da app. A raiz do repositório (docs, scripts, deploy) é da skill `project-folder-organizer`.

## Princípios

- **Reorganizar não muda comportamento.** Um commit de organização só move, renomeia e ajusta `package`/imports. Sem lógica nova, sem reformatar ficheiros inteiros, sem renomear API pública por gosto. Assim o diff é revisável e a regressão é improvável.
- **Agrupar por funcionalidade primeiro, por tipo depois.** `feature/planner/{ui,domain,data}` escala melhor do que `viewmodels/`, `repositories/`, `screens/` com tudo misturado, porque mexer numa funcionalidade toca numa só pasta.
- **Respeitar o que já existe.** Se o projeto tem uma arquitetura coerente, seguir essa. Só propor a migração para features quando há 3 ou mais funcionalidades ou quando o utilizador o pede. Num projeto pequeno, camadas simples chegam.
- **Um ficheiro, uma responsabilidade.** O nome do ficheiro diz o que lá está; se precisas de "e" para o descrever, dividir.
- **Dependências num só sentido.** `ui → domain ← data`. Uma feature não importa código interno de outra feature: o que for partilhado sobe para `core/`.
- **Verificar sempre.** Mover ficheiros sem compilar é a forma mais rápida de deixar o projeto partido.

## Fluxo de trabalho

### 1. Diagnosticar

Correr o verificador incluído para ter dados em vez de impressões:

```powershell
python .opencode/skills/app-code-organizer/scripts/check_structure.py app/src
```

Reporta: `package` que não bate com a pasta, ficheiros com mais linhas do que o limite (300 por omissão), nome do ficheiro diferente da classe que contém, e pastas com demasiados ficheiros soltos. Opções: `--max-lines`, `--max-files-per-dir`. Para projetos Python, ler a árvore com `git ls-files "*.py"` e aplicar as mesmas ideias à mão.

Ler também `build.gradle(.kts)` para obter o `namespace`/`applicationId` (package base) e `AndroidManifest.xml` para saber que classes são referenciadas por nome.

### 2. Identificar a arquitetura atual

Perceber se é por camadas, por features ou uma mistura. Anotar quais as funcionalidades reais da app (ex.: planner, wardrobe, settings). Cada funcionalidade vira uma pasta em `feature/`.

### 3. Propor o plano

Mostrar a estrutura alvo e a tabela de movimentos (origem → destino, novo package). Trabalhar em **lotes pequenos**: uma feature ou uma camada de cada vez, para poder compilar e fazer commit entre lotes.

### 4. Executar um lote

1. Mover com `git mv` para o histórico sobreviver.
2. Atualizar a linha `package ...` para bater com a nova pasta.
3. Atualizar os `import` em todos os ficheiros que usam a classe movida (`git grep -n "com.exemplo.antigo.Classe"`).
4. Atualizar referências por nome fora do código (secção "Armadilhas").
5. Compilar antes de passar ao lote seguinte.

No Android Studio, *Refactor → Move* faz os passos 2 e 3 sozinho; se o utilizador estiver a trabalhar lá, sugerir esse caminho para movimentos grandes.

### 5. Verificar

```powershell
./gradlew assembleDebug           # compila
./gradlew testDebugUnitTest       # testes unitários
./gradlew lintDebug               # opcional, apanha recursos/manifest partidos
python scripts/check_structure.py app/src   # repetir o diagnóstico
```
Python: `pytest` e `python -m compileall app`. Se algo falhar, corrigir no próprio lote em vez de avançar.

### 6. Commit por lote

Um commit por lote com mensagem clara (ex.: `refactor: mover ficheiros da feature planner para feature/planner`). Usar a skill `git-commit-workflow` se existir.

## Android / Kotlin / Compose

### Estrutura alvo (por feature)

```
app/src/main/java/<package.base>/
├── App.kt                      # classe Application
├── MainActivity.kt
├── navigation/                 # NavHost, rotas, destinos
├── core/
│   ├── common/                 # utilitários, extensões, constantes, Result/erros
│   ├── database/               # Room: AppDatabase, conversores, migrações
│   ├── network/                # Retrofit/OkHttp, interceptors
│   ├── datastore/              # preferências
│   ├── di/                     # módulos Hilt/Koin
│   ├── notification/           # canais, helpers de notificações e alarmes
│   └── designsystem/           # tema, cores, tipografia, componentes reutilizáveis
└── feature/
    └── <nome-da-feature>/
        ├── ui/                 # Screen, ViewModel, UiState, componentes locais
        ├── domain/             # modelos, casos de uso, interface do repositório
        └── data/               # implementação do repositório, DAO, entity, DTO, mapeadores
```

Pastas dentro de `feature/<x>/` só se criam quando têm conteúdo. Uma feature simples pode ter apenas `ui/` e `data/`.

### Onde vai cada tipo de ficheiro

| Ficheiro | Local | Nome |
|---|---|---|
| Ecrã Compose | `feature/x/ui/` | `XScreen.kt` |
| Componente usado só nessa feature | `feature/x/ui/components/` | `XCard.kt`, `XDialog.kt` |
| Componente usado em várias features | `core/designsystem/components/` | `AppButton.kt` |
| ViewModel | `feature/x/ui/` | `XViewModel.kt` |
| Estado / eventos do ecrã | `feature/x/ui/` | `XUiState.kt`, `XUiEvent.kt` |
| Modelo de domínio | `feature/x/domain/model/` | `X.kt` |
| Caso de uso | `feature/x/domain/usecase/` | `GetXUseCase.kt` |
| Interface do repositório | `feature/x/domain/` | `XRepository.kt` |
| Implementação do repositório | `feature/x/data/` | `XRepositoryImpl.kt` |
| Entity, DAO (Room) | `feature/x/data/local/` | `XEntity.kt`, `XDao.kt` |
| DTO, API service | `feature/x/data/remote/` | `XDto.kt`, `XApi.kt` |
| Mapeadores | `feature/x/data/mapper/` | `XMapper.kt` |
| Worker, alarme, receiver | `feature/x/` ou `core/notification/` | `XWorker.kt`, `XAlarmReceiver.kt` |
| Tema | `core/designsystem/theme/` | `Color.kt`, `Type.kt`, `Theme.kt` |
| Extensões | `core/common/extensions/` | `StringExt.kt`, `DateExt.kt` |

### Regras de nomes e tamanho

- Uma classe principal por ficheiro e o **nome do ficheiro igual ao da classe**. Exceção normal em Kotlin: `sealed class`/`interface` com as suas subclasses no mesmo ficheiro.
- Funções soltas de utilidade ficam em ficheiros com sufixo `Ext`/`Utils` e nome pelo assunto (`DateExt.kt`), nunca `Utils.kt` genérico com 800 linhas.
- Ficheiro com mais de ~300 linhas: dividir. Em Compose, partir o ecrã grande em composables menores em `components/`.
- Composables com `@Preview` só para desenvolvimento podem ficar no fim do ficheiro do composable ou em `XPreviews.kt`.
- Packages em minúsculas, sem underscores nem maiúsculas: `feature.dailyplanner`, não `feature.daily_planner`.

### Recursos (`res/`)

Os recursos são globais, por isso a organização faz-se pelo **prefixo no nome**:

- Drawables: `ic_` ícones, `img_` imagens, `bg_` fundos; prefixo da feature quando específico (`ic_planner_add`).
- Strings: `planner_title`, `wardrobe_empty_message`; prefixo da feature evita colisões.
- Layouts XML (se existirem): `activity_`, `fragment_`, `item_`, `view_`, `dialog_`.
- Uma pasta `values-pt-rPT` para textos em português europeu; evitar strings escritas diretamente no código.
- Apagar recursos não usados só depois de confirmar com o lint (*unused resources*), nunca à mão.

### Armadilhas ao mover ficheiros Android

Estas referências guardam o nome completo da classe e partem em silêncio (ou só em runtime):

- `AndroidManifest.xml`: `<activity>`, `<service>`, `<receiver>`, `<provider>`, `android:name`.
- Regras `proguard-rules.pro` / `consumer-rules.pro` que citam classes.
- Módulos DI (Hilt/Koin) e `@InstallIn`.
- Rotas de navegação e *deep links*, `NavHost` com classes serializáveis.
- Room: `@Database(entities = [...])`, esquemas exportados, migrações e conversores.
- Referências por reflexão, `Class.forName`, `WorkManager` com nome de classe guardado em BD, `PendingIntent` de alarmes já agendados (mudar o nome do receiver pode invalidar alarmes existentes: avisar).
- `serialVersionUID` / nomes usados em `Parcelable`/serialização guardada em disco.
- Testes em `src/test` e `src/androidTest` que devem seguir o **mesmo package** que a classe testada; mover os testes junto.

Se o package de uma classe persistida (BD, preferências, alarmes) mudar, dizer ao utilizador o risco antes de fazer.

## Backend Python (Flask)

### Estrutura alvo

```
app/
├── __init__.py            # create_app() (app factory)
├── config.py
├── extensions.py          # db, migrate, cache… instanciados sem app
├── models/                # SQLAlchemy: um ficheiro por entidade ou domínio
├── schemas/               # validação/serialização (marshmallow/pydantic)
├── routes/                # ou api/: blueprints por recurso (transit.py, auth.py)
├── services/              # lógica de negócio (sem Flask request/response)
├── repositories/          # acesso a dados, se a app for grande
├── utils/                 # helpers pequenos e específicos
└── exporters/             # geradores de ficheiros (ex.: Excel) por formato
tests/                     # espelha app/: tests/services/test_x.py
```

Regras: as rotas só recebem, validam e delegam; a lógica vive em `services/`; os modelos não importam rotas; nada de `utils.py` gigante. Nomes de ficheiro em `snake_case`, um blueprint por ficheiro. Cuidado com **imports circulares** ao mover: instanciar extensões em `extensions.py` e importar dentro de `create_app()`.

Ao mover módulos, atualizar: `from app.x import y`, registo de blueprints, `wsgi.py`/`gunicorn` (`app:create_app()`), `systemd` (`ExecStart`, `WorkingDirectory`) e testes.

## Outras stacks

Para Godot, Node ou outras, aplicar os mesmos princípios: agrupar por funcionalidade, nome de ficheiro = conteúdo, dependências num só sentido, mover em lotes com verificação. Ler a estrutura existente antes de propor.

## Relatório final

Terminar com: estrutura final (árvore curta), nº de ficheiros movidos, resultado do build/testes, resultado do `check_structure.py`, e o que ficou por decidir (ficheiros grandes que exigem refatoração real, riscos de persistência). Refatorações que **alteram** lógica ficam fora desta skill: propor como passo separado.
