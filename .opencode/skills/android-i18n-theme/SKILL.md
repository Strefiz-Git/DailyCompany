---
name: android-i18n-theme
description: Manage all user-facing text and the visual theme of a Kotlin Jetpack Compose Android app through Kotlin objects instead of strings.xml - the AppStrings interface, PtPtStrings/PtBrStrings/EnStrings objects, LocalStrings CompositionLocal, language switching at runtime, locale-aware date and number formatting, PT-PT vs PT-BR vocabulary, light/dark theme with ThemeMode, and persisting both in DataStore. Use this skill whenever the task adds or changes any visible text, adds a language, touches dates, plurals, the theme, colors, dark mode, or the settings for appearance and language.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, compose, material3, datastore
  languages: pt-PT, pt-BR, en
---

# Textos e tema (sistema em Kotlin)

Esta app **não usa `strings.xml`** para o texto da interface. Todos os textos vivem em objetos Kotlin,
seguindo o padrão `ui/theme/Language.kt` + `ui/theme/languages/*.kt`. Línguas suportadas:
**pt-PT (referência), pt-BR, en**. Código, identificadores, comentários e commits continuam em inglês.

## Estrutura

```
ui/theme/
├── Language.kt          # enum AppLanguage, interface AppStrings, LocalStrings, stringsFor()
├── Theme.kt             # DailyCompanyTheme(themeMode, language, dynamicColor)
├── Color.kt  Type.kt  Dimens.kt
└── languages/
    ├── PtPtStrings.kt   # referência
    ├── PtBrStrings.kt
    └── EnStrings.kt
```

## Regras

1. **Nenhum literal de texto visível num composable.** Sempre `val s = strings()` no topo e depois `s.actionSave`.
2. **Chave nova = três implementações.** Adiciona-se à interface `AppStrings` e o compilador obriga a preencher PT-PT, PT-BR e EN. Nunca deixar uma língua a apontar para outra ("TODO", texto em inglês no meio do português).
3. **Sem concatenação.** Texto com valores é uma **função**: `fun outfitsSaved(name: String)`, `fun wardrobeItemsCount(n: Int)`.
4. **Plurais são funções com `when`/`if`.** Testar sempre 0, 1 e 2.
5. **Conteúdo do utilizador nunca é traduzido** (nomes de peças, de outfits, notas). Categorias por omissão são guardadas por `nameKey` e traduzidas na apresentação; categorias criadas pelo utilizador ficam como ele escreveu.
6. **Fora do Compose** (notificações, canais, workers, receivers) usa-se `StringsProvider.current()`, que lê a língua do DataStore. Nunca `PtPtStrings` diretamente.
7. **`strings.xml` fica só para o que o sistema Android exige**: `app_name`, o rótulo do atalho e pouco mais. Nada de texto de ecrãs lá.
8. **Datas, horas e números** usam o `Locale` da língua escolhida (`LocalAppLocale`), não `Locale.getDefault()`.
9. **Acessibilidade também é texto:** `contentDescription` vem de `AppStrings` como qualquer outra chave.
10. Mudar de língua ou de tema **não reinicia a app** — é estado de Compose e a recomposição trata de tudo.

## Trade-off assumido (e como o compensamos)

Este sistema troca as ferramentas do Android (lint `MissingTranslation`, plurais por `quantity`, seleção de idioma por app no Android 13+) por segurança de compilação e troca de língua instantânea.
Compensações obrigatórias:
- Teste unitário que percorre `AppLanguage.entries` e verifica que nenhuma string está vazia.
- `Locale` forçado na `Configuration` dentro do tema, para os pickers de data/hora do Material 3 seguirem a língua da app.
- Revisão manual das três línguas sempre que se acrescentam chaves.

## Vocabulário: PT-PT vs PT-BR

| pt-PT | pt-BR |
|---|---|
| ecrã | tela |
| ficheiro | arquivo |
| guardar | salvar |
| apagar / eliminar | excluir |
| anular | desfazer |
| utilizador | usuário |
| telemóvel | celular |
| definições | configurações |
| transferir | baixar |
| pesquisar | buscar |
| gerir | gerenciar |
| peça / outfit | peça / look |
| na lavagem | na lavanderia |
| ténis | tênis |
| camisola | blusa/moletom |
| calções | bermuda |
| tamanhos S/M/L | tamanhos P/M/G |

Tom nas duas: informal e direto. PT-PT trata por **tu** ("Adiciona a tua primeira peça"); PT-BR evita o pronome ou usa **você** ("Adicione a primeira peça"). Botões com uma ou duas palavras. Erros dizem o que aconteceu e o que fazer, sem termos técnicos.

## Datas e números

```kotlin
val locale = LocalAppLocale.current
val formatter = remember(locale) {
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
}
```

- Semana começa à **segunda-feira** em PT-PT e PT-BR (`WeekFields.of(locale)`); em EN respeitar o locale.
- Relógio de 24 h por omissão nas duas línguas portuguesas; respeitar `DateFormat.is24HourFormat(context)` nos pickers.
- Nomes de dias e meses em minúscula em português; vêm do formatador, nunca de listas escritas à mão.
- Vírgula decimal em pt-PT/pt-BR, ponto em en: usar `NumberFormat.getInstance(locale)`.
- Temperatura "18 °C" com espaço.

## Tema

- `ThemeMode { LIGHT, DARK, SYSTEM }`, guardado no DataStore, aplicado em `DailyCompanyTheme`.
- Cor dinâmica (Android 12+) é opcional e **desligada por omissão**; quando ligada substitui o esquema da app.
- Zero cores, tamanhos de letra ou raios hardcoded nos ecrãs — só `MaterialTheme.colorScheme/typography/shapes`, `Spacing` e `LocalAppColors`.
- Previews com `@PreviewLightDark` e, para texto, `@PreviewFontScale`.
- Detalhes da paleta e da tipografia estão em `DESIGN.md`.

## Definições (ecrã)

Secção "Aparência": tema (Claro/Escuro/Sistema), cor dinâmica, **língua** (Seguir o sistema, Português (Portugal), Português (Brasil), English). Mudar qualquer uma aplica-se imediatamente e persiste.

## Checklist

- [ ] Nenhuma string literal visível fora de `ui/theme/languages/`.
- [ ] Chaves novas implementadas nas três línguas, com plurais tratados.
- [ ] Textos fora do Compose vêm de `StringsProvider`.
- [ ] Datas/números usam `LocalAppLocale`.
- [ ] Teste de completude das línguas passa.
- [ ] Claro e escuro verificados; nada hardcoded.
- [ ] `./gradlew :app:assembleDebug` e `:app:testDebugUnitTest` corridos e reportados.