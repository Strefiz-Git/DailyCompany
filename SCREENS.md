# SCREENS.md — DailyCompany

> Especificação dos ecrãs e da navegação. Estilo visual → `DESIGN.md` · Âmbito e regras → `PROJECT.md`.
> Cada ecrã aqui tem: **objetivo · conteúdo · ações · estados**. Se um ecrã não está aqui, não existe ainda — pergunta antes de o inventar.

---

## 1. Mapa de navegação

```
MainActivity (single Activity, NavHost)
│
├── NavigationBar (5 destinos, sempre visível nos ecrãs de topo)
│   ├── Hoje          today
│   ├── Tarefas       tasks
│   ├── Agenda        events
│   ├── Roupa         wardrobe
│   └── Outfits       outfits
│
├── task_edit?taskId={id}                 (full screen)
├── event_edit?eventId={id}               (full screen)
├── alarm_edit?alarmId={id}               (full screen)
├── item_detail/{itemId}                  (full screen)
├── item_edit?itemId={id}                 (full screen)
├── outfit_builder?outfitId={id}&random={bool}   (full screen)
├── outfit_detail/{outfitId}              (full screen)
├── categories                            (full screen, a partir de Definições ou Roupa)
├── alarms                                (lista de alarmes, a partir de Agenda ou Definições)
├── stats                                 (opcional, fase 7)
├── settings                              (full screen)
│   ├── settings_backup
│   ├── settings_appearance
│   └── settings_about
└── permissions_help                      (ajuda sobre alarmes/bateria)
```

Regras de navegação:
- Argumentos são sempre **ids** (`Long`) ou `null`; nada de objetos serializados na rota.
- Os ecrãs de edição são *full screen* com `TopAppBar` (← e ação "Guardar"), nunca diálogos.
- Voltar com formulário sujo → diálogo "Descartar alterações?".
- Deep links (fase 3+): notificação de tarefa → `task_edit`; de evento → `event_edit`; resumo diário → `today`.

## 2. Hoje (`today`) — ecrã inicial

**Objetivo:** responder às duas perguntas do dia sem nenhum toque.

Conteúdo (scroll vertical, `LazyColumn`):
1. **Cabeçalho** — saudação ("Bom dia"), data por extenso em minúsculas ("domingo, 20 de setembro"), meteorologia opcional (ícone + máx/mín).
2. **Cartão do outfit** (`shapes.large`, destaque visual do ecrã) — colagem das peças, nome se for um outfit guardado, razões curtas em chips ("Vai chover", "Formal"), e três ações: **dado** (gerar outro), **Usar hoje**, **Abrir** (vai ao construtor).
3. **Tarefas de hoje** — `SectionHeader("Hoje")` + até 5 `TaskRow`; atrasadas no topo com ponto `overdue`; "Ver todas" se houver mais.
4. **Lembretes e alarmes de hoje** — hora + etiqueta, em linha compacta.
5. **Agenda** — eventos de hoje e dos próximos 7 dias, agrupados por "hoje / amanhã / data".

Ações: FAB estendido **"Adicionar"** → bottom sheet com escolha rápida (Tarefa · Evento · Alarme · Peça) · toque em qualquer linha abre o respetivo detalhe · deslizar tarefa para concluir/adiar.

Estados: *Empty* = "Dia livre. Queres anotar alguma coisa?" (o cartão do outfit mantém-se se houver roupa) · sem roupa suficiente = cartão convida a adicionar peças.

## 3. Tarefas (`tasks`)

**Objetivo:** gerir o que há para fazer.

Conteúdo: `TopAppBar` com pesquisa · separadores **Hoje · Próximas · Sem data · Concluídas** · lista agrupada por dia.
`TaskRow`: checkbox grande · título (riscado quando feita) · linha de metadados (hora, etiqueta, ícone de repetição) · ponto de prioridade.

Ações: entrada rápida fixa no fundo (campo de texto + enviar, cria com título só) · FAB para o formulário completo · deslizar → esquerda concluir, direita adiar · toque longo → seleção múltipla.

Estados: *Empty* por separador ("Nada para hoje.", "Sem tarefas concluídas ainda.").

### 3.1 Editar tarefa (`task_edit`)
Campos por ordem: título (foco automático) · nota · data · hora · prioridade (segmented) · etiqueta · repetição (`RecurrencePicker`) · lembrete (`ReminderPicker`) · subtarefas.
Guardar desativado enquanto o título estiver vazio. Apagar no menu ⋮ com Anular.

## 4. Agenda (`events`)

**Objetivo:** datas marcadas, com e sem repetição.

Conteúdo: alternador **Lista / Calendário**.
- *Lista*: próximos eventos agrupados por mês; cada linha com dia grande, título, hora/dia inteiro, ícone de repetição, contagem para aniversários ("faz 25 anos").
- *Calendário*: mês com pontos nos dias com conteúdo; toque num dia abre a lista desse dia por baixo. Semana começa à segunda.

Ações: FAB "Novo evento" · acesso a **Alarmes** no menu · toque abre o evento.

Estados: *Empty* = "Sem eventos marcados. Marca o primeiro."

### 4.1 Editar evento (`event_edit`)
Título · data · interruptor "dia inteiro" · hora · local · notas · cor/etiqueta · **repetição** · **lembretes** (vários).
`RecurrencePicker`: Nunca · Todos os dias · Dias da semana (chips S T Q Q S S D) · Semanal · Mensal (mesmo dia / mesma posição) · Anual · Personalizada (cada N unidades) + fim (nunca / data / N ocorrências).
Ao editar uma ocorrência de série, perguntar: **só esta** ou **todas**.

### 4.2 Alarmes (`alarms` / `alarm_edit`)
Lista de alarmes com hora grande, etiqueta, dias repetidos e interruptor. Edição: hora (`TimePicker`), dias da semana, etiqueta, som, vibração, adiar automático.
Aviso no topo se faltarem permissões, com link para `permissions_help`.

## 5. Roupa (`wardrobe`)

**Objetivo:** catalogar e encontrar peças.

Conteúdo: `LargeTopAppBar` "Guarda-roupa" com contagem ("124 peças") · campo de pesquisa · `FilterChipRow` (categoria → subcategoria) · linha secundária de filtros (tamanho, cor, estação, favoritos, só disponíveis) · **grelha adaptativa** (`GridCells.Adaptive(140.dp)`) de `ClothingCard` 3:4.

Ações: FAB "Adicionar peça" · toque → detalhe · toque longo → seleção múltipla (marcar na lavagem, adicionar categoria, apagar com Anular) · menu ⋮ → ordenação e **Gerir categorias**.

Estados: *Empty* global = "Ainda não tens peças. Adiciona a primeira." · *Empty* filtrado = "Nada com estes filtros." + "Limpar filtros".

### 5.1 Adicionar/editar peça (`item_edit`)
Ordem do formulário: **foto** (grande, toque abre escolha Galeria/Câmara) → **nome** → **categorias** (`CategoryPicker`, multi, agrupado por categoria pai) → **tamanho** (`SizePicker`: sistema + valor, com opção "Personalizado") → opcionais colapsáveis (marca, cores, estações, agasalho, formalidade, impermeável, notas).
Obrigatório: nome + ≥1 categoria. O slot é pré-preenchido pela categoria e editável. Estado do formulário sobrevive a rotação.

### 5.2 Detalhe da peça (`item_detail`)
Foto grande · nome · chips de categorias e tamanho · atributos · "Usada N vezes, última vez a …" · lista de outfits que a incluem · ações: favorito, na lavagem, editar, apagar (avisa quantos outfits afeta).

## 6. Outfits (`outfits`)

**Objetivo:** gerar, guardar e reutilizar conjuntos.

Conteúdo: pesquisa · filtros (favoritos, formalidade) · grelha de cartões com `OutfitCollage`, nome, nº de peças e última utilização.
Ações: **botão do dado** em destaque (FAB estendido "Outfit aleatório") · FAB secundário "Criar outfit" · toque → detalhe · toque longo → duplicar/apagar.

Estados: *Empty* = "Sem outfits guardados. Experimenta o botão dos dados."

### 6.1 Construtor / aleatório (`outfit_builder`)
- **Pré-visualização** no topo: as peças escolhidas empilhadas.
- Uma linha por slot: Top · Fundo *(ou Peça única, alternável)* · Casaco · Calçado · Acessórios. Cada linha é um carrossel horizontal das peças desse slot, com **cadeado** à esquerda e estado vazio que liga a "Adicionar peça".
- Barra inferior: **Outro** (dado) · **Guardar** (abre `NameInputDialog`) · **Usar hoje**.
- Menu de filtros (bottom sheet): formalidade, estação, só favoritos, incluir casaco (auto/sempre/nunca), usar meteorologia.
- Razões da sugestão em chips por baixo da pré-visualização.
- Se faltar um slot obrigatório: mensagem clara do que adicionar, sem outfit parcial silencioso.

### 6.2 Detalhe do outfit (`outfit_detail`)
Colagem grande · nome · lista das peças (toque abre a peça) · notas · "usado N vezes" · ações: Usar hoje, Editar, Renomear, Duplicar, Favorito, Apagar (Anular).

## 7. Categorias (`categories`)
Árvore de dois níveis, arrastável para reordenar. Adicionar categoria e subcategoria, renomear, apagar (avisa quantas peças perdem a categoria; nunca apaga peças). Repor categorias por omissão.

## 8. Definições (`settings`)
Secções: **Aparência** (tema Claro/Escuro/Sistema, cor dinâmica, **língua**: Seguir o sistema · Português (Portugal) · Português (Brasil) · English — aplicadas de imediato) · **Dia** (hora do resumo diário, primeiro dia da semana, formato de hora) · **Outfits** (dias a evitar repetir, incluir casaco por omissão, meteorologia e cidade) · **Guarda-roupa** (gerir categorias) · **Dados** (`settings_backup`) · **Ajuda** (alarmes e bateria) · **Sobre** (versão, licenças).

### 8.1 Dados / cópia de segurança (`settings_backup`)
Dois cartões:
- **Exportar dados** — explica o que vai no ficheiro, botão abre o `CreateDocument("application/zip")` do sistema com nome sugerido `dailycompany-backup-AAAA-MM-DD-HHmm.zip`. No fim, mensagem com as contagens e aviso se alguma foto faltou.
- **Importar dados** — botão abre `OpenDocument(["application/zip"])`. Depois de escolher, um diálogo pergunta a estratégia: **Juntar** (por omissão) ou **Substituir tudo**; esta última pede uma segunda confirmação explícita. Durante o processo, indicador de progresso e botões desativados. Erros possíveis com mensagem própria: ficheiro inválido, versão não suportada, falha a meio (nada é gravado).

Nota: sem nuvem e sem conta, este ecrã é a única forma de passar os dados para outro telemóvel — deve ser fácil de encontrar e estar explicado numa frase.

## 9. Ecrãs de sistema e transversais
- **Pedidos de permissão:** notificações (antes do primeiro lembrete), alarmes exatos (antes do primeiro alarme), câmara (só se for usada câmara própria). Sempre com uma frase de contexto em PT-PT antes do diálogo do sistema.
- **`permissions_help`:** explicação e atalhos para as definições do sistema quando os alarmes não disparam.
- **Snackbar de Anular:** host único no `Scaffold`, 5 s, nunca tapando o FAB.
- **Notificações:** resumo diário (Guia do dia) · lembrete de tarefa/evento (ações Feito/Adiar) · alarme (alta prioridade, ecrã de bloqueio).

## 10. Ordem de construção dos ecrãs

Fase 0: esqueleto de navegação com os 5 destinos vazios + tema.
Fase 1: `tasks`, `task_edit`, versão mínima de `today`.
Fase 2: `events`, `event_edit`.
Fase 3: `alarms`, `alarm_edit`, `permissions_help`, notificações.
Fase 4: `wardrobe`, `item_edit`, `item_detail`, `categories`.
Fase 5: `outfits`, `outfit_builder`, `outfit_detail`.
Fase 6: `today` completo com cartão de outfit e resumo diário.
Fase 7: `stats`, polimento.
Fase 8: `settings_backup`.