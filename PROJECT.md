# PROJECT.md — DailyCompany

> Documento-mestre do projeto. Define **o que é**, **para que serve**, **o que faz** e **até onde vai**.
> Design visual → `DESIGN.md` · Ecrãs e navegação → `SCREENS.md` · Regras para o agente → `AGENTS.md`.

---

## 1. Ideia original (nas palavras do autor)

> Uma app de ajuda diária, com escolha de roupa, alarmes para horários específicos, tipo guia do que fazer no dia, anotar o que tem de fazer no dia, marcar eventos em datas específicas que podem ou não repetir mais do que uma vez ao ano.
>
> E também: organizares a roupa que vais vestir, ter um botão de *random* para dar um outfit aleatório, e poderes guardar os outfits com nome. Dar upload das fotos das roupas e dar-lhes nome, categoria(s) e subcategorias, e tamanho (S, M, L, XL e etc.) ou, se necessário, tamanhos mais específicos.

Esta secção **não se altera**. É a referência final quando houver dúvida sobre o âmbito.

## 2. Em uma frase

**DailyCompany é um companheiro diário offline para Android que responde a duas perguntas todos os dias: "o que tenho de fazer hoje?" e "o que visto hoje?".**

## 3. Porquê

As apps de tarefas não sabem nada da tua roupa. As apps de guarda-roupa não sabem nada do teu dia. Aqui as duas coisas vivem juntas: o evento formal de quinta-feira influencia o outfit sugerido, a chuva de amanhã influencia o casaco, e o resumo da manhã diz-te as duas coisas de uma vez, num único ecrã.

## 4. Princípios de produto (decidem empates)

1. **Offline-first.** Funciona em modo de avião. Sem contas, sem servidores, sem tracking.
2. **Os dados são do utilizador.** Exportáveis a qualquer momento num ficheiro que ele controla.
3. **Visual primeiro.** As fotos das roupas são o conteúdo, não decoração.
4. **Uma mão, três segundos.** As ações do dia-a-dia estão ao alcance do polegar.
5. **Nunca um beco sem saída.** Toda a lista vazia explica o que fazer e oferece a ação.
6. **Em três línguas, à escolha.** pt-PT (referência), pt-BR e inglês, e tema claro/escuro — trocados na hora, sem reiniciar a app.
7. **Regras, não IA.** As sugestões são determinísticas, explicáveis e testáveis.
8. **Calma.** Sem gamificação, sem streaks, sem culpa por não cumprir. Adiar é uma ação de primeira classe.
9. **Zero fricção na entrada.** Abre e usa. Nada de onboarding obrigatório com 8 passos.

## 5. Não-objetivos (fora de âmbito)

Sincronização cloud · contas de utilizador · partilha social · e-commerce ou links de compra · integração com Google Calendar (fase futura, nunca requisito) · versão iOS · widgets na v1 · IA generativa.

## 6. Utilizador

Utilizador único, uso pessoal diário. Não há multi-perfil, não há partilha. A app fala pt-PT, pt-BR ou inglês, conforme a escolha (por omissão segue o sistema).
Momentos de uso:
- **Manhã (30 s):** ver o dia + decidir a roupa.
- **Ao longo do dia (5 s):** riscar uma tarefa, adiar, ver o próximo lembrete.
- **Noite (2 min):** anotar o de amanhã, marcar um evento.
- **Ocasional (15 min):** fotografar e catalogar roupa nova, montar e guardar outfits.

## 7. Histórias de utilizador (o que tem de ser possível)

**Dia**
- Abro a app e vejo, sem tocar em nada, o que tenho para hoje e o que vestir.
- Escrevo uma tarefa em menos de 5 segundos, sem escolher categoria nem prioridade.
- Adio uma tarefa para amanhã com um toque.
- Marco uma tarefa como feita e posso anular se me enganei.

**Eventos**
- Marco o aniversário de alguém e ele aparece todos os anos sem eu voltar a mexer.
- Marco uma consulta daqui a 3 meses e sou avisado 1 dia antes.
- Crio um evento que repete de 3 em 3 semanas, ou às segundas e quartas, ou até uma data.

**Alarmes**
- Ponho um alarme para as 07:15 nos dias úteis e ele toca mesmo com o telemóvel em repouso.
- Depois de reiniciar o telemóvel, os alarmes continuam lá.

**Guarda-roupa**
- Tiro foto a uma camisola, dou-lhe nome, marco "Tops > Camisolas" e "Inverno", tamanho M — está guardada.
- Uma peça pode estar em várias categorias ao mesmo tempo.
- Filtro o guarda-roupa por categoria, tamanho, cor ou estação; pesquiso por nome.
- Marco uma peça como "na lavagem" e ela deixa de aparecer nas sugestões.

**Outfits**
- Carrego no botão dos dados e recebo um outfit completo.
- Bloqueio os ténis, volto a gerar, e só o resto muda.
- Guardo o resultado com um nome ("Trabalho — frio") e reutilizo-o.
- Digo "usei hoje" e a app deixa de mo sugerir nos dias seguintes.

**Dados, línguas e tema**
- Exporto tudo para um ficheiro `.zip` e recupero-o num telemóvel novo, com as fotos incluídas.
- Mudo de telemóvel sem perder nada, mesmo sem nuvem nem conta.
- Escolho a língua e o tema nas definições e a app muda na hora.

## 8. Funcionalidades (âmbito detalhado)

### 8.1 Hoje — o guia do dia
Ecrã inicial. Secções, por ordem:
1. Cabeçalho: saudação + data por extenso + (opcional) meteorologia.
2. **Cartão do outfit:** sugestão de hoje com miniaturas das peças, razão curta ("Vai chover: casaco impermeável"), botão de dados (gerar outro) e ação "Usar hoje".
3. **Tarefas de hoje** (atrasadas primeiro, marcadas a cor de aviso).
4. **Próximos lembretes/alarmes** do dia, com hora.
5. **Eventos** de hoje e dos próximos 7 dias.
6. Entrada rápida de tarefa sempre acessível (FAB).
- Estado vazio: "Dia livre. Queres anotar alguma coisa?" + ação.
- Notificação opcional de resumo diário a uma hora escolhida.

### 8.2 Tarefas
- Criar com **só um título** (tudo o resto é opcional): nota, data, hora, prioridade (nenhuma/média/alta), etiqueta, subtarefas.
- Repetição simples: diária, dias da semana, semanal, mensal.
- Ações: concluir, adiar (+1 dia / próxima semana / escolher data), editar, apagar com **Anular**.
- Vistas: Hoje · Próximas · Sem data · Concluídas (agrupadas por dia).
- Tarefas por concluir de dias anteriores **transitam** para Hoje marcadas como atrasadas (nunca desaparecem em silêncio).

### 8.3 Eventos e datas
- Campos: título, data, hora ou dia inteiro, local (texto livre), notas, cor/etiqueta.
- **Repetição:** nenhuma · diária · dias da semana · semanal · mensal (mesmo dia ou mesma posição, ex. "3.ª sexta") · **anual** · personalizada (cada N dias/semanas/meses/anos). Fim: nunca · numa data · após N ocorrências. Exceções: apagar/alterar uma ocorrência isolada.
- Casos que têm de estar certos: 29 de fevereiro, dia 31 em meses curtos, mudança de hora legal, fusos (usar sempre hora local).
- Lembretes: no momento · 10 min · 30 min · 1 h · 1 dia · N dias antes · hora personalizada na véspera.
- Para eventos anuais, mostrar a idade/contagem ("faz 25 anos", "3.º aniversário").
- Vistas: lista de próximos (agrupada por mês) e calendário mensal com pontos nos dias com conteúdo.

### 8.4 Alarmes e lembretes
- Alarmes com hora, etiqueta, repetição por dias da semana, som e vibração, ativar/desativar por interruptor.
- Notificações com ações rápidas: **Feito**, **Adiar 10 min**, **Ver**.
- Canais separados: *Alarmes* (alta prioridade, som) · *Lembretes* · *Guia do dia*.
- Reagendamento obrigatório após: reinício do telemóvel, atualização da app, mudança de hora, restauro de backup.
- Permissões pedidas no momento certo e com explicação: notificações (Android 13+), alarmes exatos (Android 12+), otimização de bateria (só sugerida, com instruções).

### 8.5 Guarda-roupa
- **Adicionar peça:** foto (galeria ou câmara) → nome → categorias/subcategorias (várias) → tamanho. Obrigatório: nome + pelo menos uma categoria. Foto fortemente recomendada.
- **Tamanhos:** letras (XXS…XXXL) · numérico EU · calçado EU (35–48, meios números) · cintura/comprimento ("32/34") · tamanho único · **personalizado** em texto livre. Sistema escolhido por peça, pré-preenchido pela categoria.
- Atributos opcionais: marca, cores (multi), estações, nível de agasalho (1–5), formalidade, impermeável, notas.
- Estados: favorito · disponível/na lavagem · arquivada.
- Organização: grelha de fotos adaptativa, filtros (categoria → subcategoria, tamanho, cor, estação, favoritos, só disponíveis), pesquisa por nome/marca, ordenação (recente, nome, mais usada, menos usada), seleção múltipla com ações em lote.
- **Categorias por omissão** (editáveis, renomeáveis, reordenáveis, com no máximo 2 níveis):
  Tops · Fundos · Peça única · Casacos · Calçado · Acessórios, cada uma com subcategorias (ver `AGENTS.md` e a skill `android-wardrobe-outfits` para a lista completa).

### 8.6 Outfits
- **Construtor por slots:** Top · Fundo *(ou Peça única)* · Casaco · Calçado · Acessórios (0–2). Cada slot é um carrossel das peças disponíveis, com **cadeado** para fixar.
- **Botão aleatório:** gera um outfit completo respeitando cadeados e filtros; mostra as razões; "Outro" volta a gerar sem repetir o que já saiu na sessão.
- Filtros do gerador: formalidade, estação, só favoritos, incluir casaco (auto/sempre/nunca), usar meteorologia.
- **Guardar com nome** (obrigatório, ≤ 40 caracteres); listar, pesquisar, renomear, duplicar, editar, favoritar, apagar com Anular.
- **"Usar hoje"** regista o uso: incrementa contadores, grava no histórico, e o outfit/peça deixa de ser sugerido durante N dias (por omissão 4).
- Estatísticas simples: peça mais usada, peça nunca usada, outfit mais repetido.

### 8.7 Definições
**Aparência:** tema claro/escuro/sistema, cor dinâmica (desligada por omissão), **língua** (seguir o sistema, pt-PT, pt-BR, inglês) — tudo aplicado na hora, sem reiniciar a app.
**Dia:** hora do resumo diário, primeiro dia da semana, formato de hora.
**Outfits:** dias a evitar repetir, casaco por omissão, meteorologia.
**Guarda-roupa:** gerir categorias. **Dados:** exportar/importar. **Sobre:** versão e licenças.

### 8.8 Backup
Enquanto não houver sincronização na nuvem, o backup é a forma de **mudar de telemóvel e ficar tudo igual**: um único ficheiro `.zip` com **absolutamente tudo** — guarda-roupa, outfits, tarefas, eventos, lembretes, histórico de uso, **e também as definições** (tema, língua, preferências de outfit) — mais as fotos. Não há backup parcial nem um ficheiro separado só de fotos: é tudo junto, sempre.
Formato: `manifest.json` (app, versão do esquema, contagens) + `data.json` (`BackupData`: entidades ligadas por `uid` + `BackupSettings`: tema/língua/preferências) + `images/`. O picker é do sistema (SAF), por isso não é preciso nenhuma permissão de armazenamento. A importação valida tudo antes de escrever, corre numa transação para os dados, aplica sempre as definições, e no fim recalcula todos os alarmes. Ao abrir o ficheiro num telemóvel novo, a app fica com o mesmo aspeto (tema, língua) e o mesmo conteúdo do telemóvel antigo. Detalhe na skill `android-backup-restore`.

## 9. Regras de negócio que não podem falhar

| # | Regra |
|---|---|
| R1 | Uma peça tem **várias categorias** mas **exatamente um slot** funcional. |
| R2 | Peças indisponíveis ("na lavagem") nunca entram numa sugestão aleatória. |
| R3 | O gerador nunca devolve Top+Fundo **e** Peça única ao mesmo tempo. |
| R4 | Sem candidatos para um slot obrigatório → mensagem explicativa, nunca um outfit incompleto silencioso. |
| R5 | Apagar uma peça usada em outfits avisa quantos outfits afeta antes de confirmar. |
| R6 | Uma ocorrência de evento nunca é gravada na BD; é sempre calculada a partir da regra. |
| R7 | Qualquer apagar tem **Anular** durante pelo menos 5 segundos. |
| R8 | Tarefa por concluir nunca desaparece: transita para Hoje como atrasada. |
| R9 | Depois de restaurar um backup, todos os alarmes são recalculados antes de dizer "concluído". |
| R10 | Nenhum texto visível escrito à mão num ecrã: tudo passa por `AppStrings`, nas três línguas. |
| R11 | Mudar de língua ou de tema aplica-se de imediato e sobrevive a fechar a app. |
| R12 | Uma importação só é dada como concluída depois de os alarmes serem recalculados. |
| R13 | O backup nunca é só as fotos: dados de domínio e definições da app viajam sempre juntos no mesmo ficheiro. |

## 10. Modelo de dados (visão geral)

Todas as entidades exportáveis têm `uid: String` (UUID) estável + `createdAt`/`updatedAt`.

```
CategoryEntity(id, uid, parentId?, name, nameKey?, slot?, sortOrder)
ClothingItemEntity(id, uid, name, slot, imageFile?, thumbFile?, sizeSystem, sizeValue?, sizeRank?,
                   brand?, colors, seasons, warmth, formality, isWaterproof, notes?,
                   isFavorite, isAvailable, isArchived, lastWornDate?, timesWorn, createdAt)
ItemCategoryCrossRef(itemId, categoryId)                       // N:N
OutfitEntity(id, uid, name, notes?, formality?, isFavorite, createdAt, lastWornDate?, timesWorn)
OutfitItemCrossRef(outfitId, itemId, slot)
WearLogEntity(id, date, outfitId?, itemIds)

TaskEntity(id, uid, title, note?, dueDate?, dueTime?, priority, tag?, isDone, doneAt?,
           recurrence?, parentTaskId?, sortOrder, createdAt)
EventEntity(id, uid, title, date, time?, isAllDay, location?, notes?, colorKey?,
            recurrence?, recurrenceEnd?, recurrenceCount?, createdAt)
EventExceptionEntity(id, eventUid, originalDate, isCancelled, overrideDate?, overrideTime?)
ReminderEntity(id, uid, targetType, targetUid?, triggerAt, offsetMinutes?, isEnabled,
               daysOfWeek?, label?, soundKey?, vibrate)
```

Domínio (puro Kotlin, sem Room): `ClothingItem`, `Outfit`, `Task`, `Event`, `RecurrenceRule`, `Slot`, `SizeSystem`, `Formality`, `Season`, `ColorKey`.

## 11. Arquitetura

`ui/` → `domain/` → `data/`, MVVM + fluxo unidirecional, single-Activity com Navigation Compose.
Stack: Kotlin · Compose · Material 3 · Room · DataStore · Hilt · WorkManager · AlarmManager · Coil · kotlinx.serialization · java.time (desugaring).
Detalhe da estrutura de pastas em `AGENTS.md` §2.

## 12. Roadmap

| Fase | Objetivo | Pronto quando |
|---|---|---|
| **0 — Fundações** | Projeto, Gradle + version catalog, Hilt, tema e design tokens, navegação, Room vazio, strings PT-PT, `.gitignore` | App corre, 5 ecrãs vazios navegáveis, claro/escuro a funcionar |
| **1 — Tarefas** | CRUD, Hoje básico, adiar, undo, estados vazios | Consigo gerir um dia inteiro só com tarefas |
| **2 — Eventos** | Eventos + regras de repetição + calculadora de ocorrências testada + lista e calendário | Aniversário anual e consulta a 3 meses funcionam |
| **3 — Lembretes e alarmes** | AlarmManager, notificações com ações, permissões, boot receiver | Alarme toca; sobrevive a reinício |
| **4 — Guarda-roupa** | Fotos (picker/câmara/EXIF/thumbnails), peça completa, grelha, filtros, pesquisa, categorias editáveis | 30 peças catalogadas sem lentidão |
| **5 — Outfits** | Construtor, gerador aleatório com cadeados, guardar com nome, usar hoje, histórico | Botão de dados dá outfits plausíveis e guardáveis |
| **6 — Guia do dia** | Hoje completo (tarefas + eventos + lembretes + outfit), notificação de resumo | Um único ecrã responde às duas perguntas |
| **7 — Polimento** | Acessibilidade, animações, desempenho, testes de UI, revisão de textos | Checklists de `DESIGN.md` todas verdes |
| **8 — Backup e release** | Exportar/importar, assinatura, R8, AAB | Restauro num telemóvel limpo fica idêntico |
| **Extras** | Meteorologia real, estatísticas, widget, inglês, arquivo de peças por estação | Só depois do essencial estável |

Cada fase acaba com: compila · testes passam · commit na branch própria · merge para `main`.

## 13. Qualidade — definição de "feito"

Uma funcionalidade só está feita quando: compila · tem testes da lógica pura · tem estados Loading/Content/Empty/Error · textos via `AppStrings` nas três línguas · funciona em claro e escuro · funciona a 200% de escala de texto · tem `contentDescription` no que interessa · apagar tem Anular · foi testada (ou ficou explicitamente declarado o que não foi testado em dispositivo).

## 14. Riscos e como os mitigar

| Risco | Mitigação |
|---|---|
| Fabricantes matam alarmes em background | AlarmManager exato + reagendamento no boot + ecrã de ajuda sobre otimização de bateria |
| Fotos grandes = OutOfMemory / app lenta | Redimensionar para ≤1600 px + thumbnails ~400 px, decode fora da main thread, grelhas só com thumbnails |
| Repetições de datas mal calculadas | Calculadora pura, isolada, com bateria de testes (29/2, dia 31, DST, fins de mês) |
| Gerador aleatório dá resultados absurdos | Pontuação ponderada + regras duras (R2–R4) + razões visíveis + testes com seed fixa |
| Perda de dados do utilizador | Backup manual completo + `uid` estáveis + import transacional e validado |
| Âmbito a crescer sem fim | Secção 5 (não-objetivos) e roadmap por fases; extras só depois da fase 8 |

## 15. Decisões tomadas

Kotlin + Compose + M3 · offline total · Room + DataStore · Hilt · sugestões por regras · fotos em armazenamento privado (BD guarda só o nome do ficheiro) · código em inglês · `minSdk 26` · git com commits descritivos e branches para mudanças grandes.
**Línguas em objetos Kotlin** (`AppStrings` + `PtPtStrings`/`PtBrStrings`/`EnStrings`) em vez de `strings.xml`: troca-se o lint do Android por segurança de compilação (falta uma chave = não compila) e troca instantânea de língua. **Tema claro/escuro** com `ThemeMode` guardado em DataStore.
**Backup completo em `.zip`** (dados de domínio + definições da app + fotos, tudo junto), por SAF, sem permissões, como substituto da nuvem para trocar de telemóvel.

## 16. Decisões em aberto

- Nome comercial e ícone (repo: `DailyCompany`; proposta de nome visível: **Daily**).
- Meteorologia: API externa (exige internet + leitura dos termos) *ou* escolha manual "frio/ameno/quente".
- Recorte manual de fotos / remoção de fundo: só se compensar o custo.
- Widget do ecrã inicial: pós-v1. Sincronização com Google Drive: só depois do backup manual estar sólido.

## 17. Glossário

**Peça** – artigo de roupa · **Slot** – função da peça no outfit (top, fundo, calçado…) · **Categoria** – organização escolhida pelo utilizador (2 níveis) · **Outfit** – conjunto guardado com nome · **Ocorrência** – uma data concreta gerada por uma regra de repetição · **Guia do dia** – ecrã Hoje · **Anular** – undo.