---
name: project-folder-organizer
description: Organiza a estrutura de pastas de um projeto ao nível do repositório (raiz, docs, scripts, assets, módulos, backend/app). Usar SEMPRE que o utilizador pedir para organizar, arrumar, limpar ou reestruturar as pastas do projeto, quando houver ficheiros soltos na raiz, ao criar a estrutura inicial de um projeto novo, ao separar docs/scripts/assets, ou quando disser que o repo está uma confusão — mesmo sem dizer "estrutura". Para organizar o código DENTRO da app (packages, camadas, features) usar a skill app-code-organizer. EN - organize repo layout, clean root directory, project scaffolding.
---

# Organizador de pastas do projeto

Objetivo: deixar a raiz do repositório limpa e previsível, para que qualquer pessoa (ou agente) perceba em 10 segundos onde está cada coisa. Esta skill trata do **exterior** do código: raiz, pastas de topo, docs, scripts, assets, módulos. O interior da app (packages Kotlin, camadas, features) é tratado pela skill `app-code-organizer`.

## Princípios

- **Propor antes de mover.** Mostrar sempre o plano (tabela origem → destino) e só executar depois de o utilizador confirmar, exceto se ele já tiver dito para avançar.
- **Preservar histórico.** Em ficheiros versionados usar `git mv`, nunca copiar + apagar. Assim o `git log --follow` continua a funcionar.
- **Nunca apagar.** Lixo, builds e temporários vão para o `.gitignore`, não para o lixo. Se algo parecer mesmo inútil, listar no relatório e deixar o utilizador decidir.
- **Uma responsabilidade por pasta.** Se uma pasta precisa de "e" no nome (`docs_e_scripts`), dividir.
- **Estrutura pequena e rasa.** Máximo 3 níveis de profundidade nas pastas de topo, salvo dentro do código-fonte (onde o layout é ditado pela linguagem/framework).
- **Não partir o build.** Mover ficheiros sem atualizar referências é a forma mais comum de estragar um projeto. O passo 5 e 6 não são opcionais.

## Fluxo de trabalho

### 1. Inspecionar sem ruído

Não usar `tree /f` na raiz: `node_modules`, `build`, `.gradle` e `.git` geram milhares de linhas inúteis. Preferir o que está versionado:

```powershell
git ls-files                      # tudo o que o git conhece
git status --short                # o que está solto / por versionar
Get-ChildItem -Force              # conteúdo da raiz (PowerShell)
```

Se não for um repositório git, listar a raiz e as pastas de topo com `Get-ChildItem` / `ls`, ignorando as pastas da lista "zonas intocáveis" abaixo.

### 2. Identificar o tipo de projeto

Ler os ficheiros de raiz para perceber o que é: `settings.gradle(.kts)` → Android/Gradle; `requirements.txt` / `pyproject.toml` → Python; `project.godot` → Godot; `package.json` → Node; `AGENTS.md` / `.opencode/` → ferramentas de agente. Um repo pode ser vários tipos (monorepo). Respeitar o que já existe: se o projeto já tem uma convenção coerente, seguir essa em vez de impor outra.

### 3. Classificar cada item da raiz

Para cada ficheiro ou pasta solta, decidir uma de quatro ações: **ficar** (pertence à raiz), **mover** (tem casa óbvia), **ignorar** (vai para `.gitignore`), **perguntar** (não é claro). Ver as tabelas de destino abaixo.

### 4. Apresentar o plano

Formato:

```
| Origem                  | Destino                   | Motivo                       |
|-------------------------|---------------------------|------------------------------|
| deploy.sh               | scripts/deploy.sh         | script utilitário            |
| ecra-inicial.png        | docs/assets/ecra-inicial.png | imagem usada no README    |
| notas-antigas.txt       | (perguntar)               | não é claro se ainda é útil  |
```

Indicar também quais referências vão ter de ser atualizadas (passo 5).

### 5. Executar e atualizar referências

```powershell
git mv origem destino             # versionados
Move-Item origem destino          # não versionados
```

Depois de mover, procurar e corrigir todas as referências ao caminho antigo:

```powershell
git grep -n "caminho/antigo"      # scripts, README, CI, gradle, configs
```

Sítios onde caminhos costumam estar escritos: `README.md`, `AGENTS.md`, `.github/workflows/`, `settings.gradle(.kts)` (`include`/`project(...).projectDir`), `build.gradle(.kts)`, scripts `.sh`/`.ps1`, `systemd`/`nginx` (`WorkingDirectory`, `root`), `Dockerfile`, `.gitignore`, imports Python relativos.

### 6. Verificar

Correr o comando de build/teste adequado ao projeto (`./gradlew assembleDebug`, `pytest`, `python -m compileall .`, etc.) e `git status` para confirmar que só há renomeações (`R`) e não apagar + criar. Se o build falhar por causa dos caminhos, corrigir antes de terminar.

### 7. Commit separado

Uma reorganização é um commit só de reorganização, sem misturar alterações de comportamento. Se existir a skill `git-commit-workflow`, usá-la para a mensagem (ex.: `chore: reorganizar estrutura de pastas do projeto`).

## O que fica sempre na raiz

Estes ficheiros têm de estar na raiz porque as ferramentas os procuram lá. Não mover:

`README.md`, `LICENSE`, `AGENTS.md`, `.gitignore`, `.gitattributes`, `.editorconfig`, `settings.gradle(.kts)`, `build.gradle(.kts)`, `gradle.properties`, `gradlew`, `gradlew.bat`, `gradle/`, `requirements.txt`, `pyproject.toml`, `package.json`, `project.godot`, `Dockerfile`, `.github/`, `.opencode/`, `.env.example`.

## Onde vai cada coisa

| Tipo de ficheiro | Destino |
|---|---|
| Documentação, decisões, guias | `docs/` (imagens em `docs/assets/`) |
| Scripts de build, deploy, manutenção | `scripts/` (sub-pastas `scripts/windows/` e `scripts/linux/` só se houver muitos) |
| Configs de servidor (nginx, systemd) | `deploy/` ou `infra/` |
| Imagens/ícones fonte, mockups, design | `design/` ou `assets/` (não confundir com `res/` da app Android) |
| Dados de exemplo, fixtures | `data/` ou `tests/fixtures/` |
| Backups, exports antigos | fora do repo, ou `.gitignore` |
| Ficheiros de segredos (`.env`, `*.jks`, `keystore.properties`, `google-services.json` privado) | **não versionar**. Confirmar `.gitignore` e avisar o utilizador se estiverem versionados |

## Layouts de referência

Usar como ponto de partida, adaptar ao que já existe.

**App Android (módulo único)**
```
projeto/
├── app/                  # código e recursos da app
├── docs/
├── scripts/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
└── AGENTS.md
```

**Android multi-módulo**
```
projeto/
├── app/                  # só orquestra: Application, navegação, DI
├── core/                 # partilhado (ui, data, common)
├── feature/              # um módulo por funcionalidade
├── docs/
├── scripts/
└── (ficheiros gradle na raiz)
```

**Backend Python (Flask)**
```
projeto/
├── app/                  # pacote da aplicação (ver app-code-organizer)
├── tests/
├── migrations/
├── scripts/
├── deploy/               # systemd, nginx
├── docs/
├── requirements.txt
└── README.md
```

**Monorepo com vários projetos**
```
repo/
├── apps/
│   ├── android-planner/
│   └── android-wardrobe/
├── backend/
├── docs/
├── scripts/
└── README.md
```
Cada projeto dentro de `apps/` segue o seu próprio layout de tipo (acima). Só criar monorepo se o utilizador já tem vários projetos no mesmo repositório.

## Convenções de nomes

- Pastas de topo: minúsculas, sem espaços, `kebab-case` (`android-planner`) ou uma palavra (`docs`).
- Sem acentos nem espaços em nomes de ficheiros e pastas (evita problemas entre Windows, Linux e CI).
- Nomes descritivos em vez de `novo`, `final`, `final2`, `teste`, `copia`. Se existirem, perguntar antes de renomear.
- Manter a língua do projeto coerente (não misturar `docs/guias` com `docs/guides`).

## Zonas intocáveis

Nunca reorganizar nem listar em detalhe: `.git/`, `node_modules/`, `build/`, `.gradle/`, `.idea/`, `.venv/`, `venv/`, `__pycache__/`, `.godot/`, `local.properties`. São geradas ou pertencem a ferramentas. Se aparecerem versionadas por engano, propor removê-las do índice (`git rm -r --cached`) e adicioná-las ao `.gitignore`, mas só depois de confirmar com o utilizador.

## Relatório final

Terminar sempre com um resumo curto: o que foi movido (contagem e destinos principais), que referências foram atualizadas, resultado do build/testes, e o que ficou por decidir. Sugerir a skill `app-code-organizer` se o código interno da app também estiver desorganizado.
