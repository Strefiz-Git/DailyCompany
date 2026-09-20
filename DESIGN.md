# DESIGN.md — DailyCompany

> Identidade visual, tokens e regras de UI. Quem escreve UI segue este ficheiro **e** a skill `android-compose-ui-design`.
> Ecrãs concretos → `SCREENS.md`.

---

## 1. Personalidade

**Calmo, quente, visual.** Uma gaveta bem arrumada, não um painel de controlo.

| É | Não é |
|---|---|
| Quente, terroso, acolhedor | Azul corporativo, frio |
| Fotografia grande, texto discreto | Texto denso, listas cinzentas |
| Espaço a respirar | Densidade máxima de informação |
| Movimento subtil, rápido | Animações longas ou decorativas |
| Neutro e silencioso quando não há nada a fazer | Badges vermelhos, streaks, culpa |

Palavras-guia: **manhã · tecido · papel · madeira clara · luz natural**.

## 2. Cor

Seed: **terracota `#B5623C`**. Secundária: **salva `#6E7F6A`**. Terciária (acentos/datas): **âmbar `#C08A2E`**.
Fundo claro = papel quente, não branco puro. Fundo escuro = carvão quente, não preto.

Regras:
- Nunca cores hardcoded nos ecrãs — só `MaterialTheme.colorScheme`.
- Cor nunca transmite estado sozinha: sempre com ícone ou texto.
- Dynamic color (Android 12+) é **opcional e desligado por omissão** — a identidade da app vem primeiro; oferece-se como interruptor nas Definições.
- As fotos das peças são a cor principal de qualquer ecrã de guarda-roupa: o cromatismo da UI fica contido para não competir.

### `ui/theme/Color.kt`

```kotlin
package com.dailycompany.ui.theme

import androidx.compose.ui.graphics.Color

// Light
val md_light_primary = Color(0xFF8F4B2A)
val md_light_onPrimary = Color(0xFFFFFFFF)
val md_light_primaryContainer = Color(0xFFFFDBCB)
val md_light_onPrimaryContainer = Color(0xFF360E00)
val md_light_secondary = Color(0xFF52634F)
val md_light_onSecondary = Color(0xFFFFFFFF)
val md_light_secondaryContainer = Color(0xFFD5E8CF)
val md_light_onSecondaryContainer = Color(0xFF101F10)
val md_light_tertiary = Color(0xFF7A5900)
val md_light_onTertiary = Color(0xFFFFFFFF)
val md_light_tertiaryContainer = Color(0xFFFFDF9B)
val md_light_onTertiaryContainer = Color(0xFF261A00)
val md_light_error = Color(0xFFBA1A1A)
val md_light_onError = Color(0xFFFFFFFF)
val md_light_errorContainer = Color(0xFFFFDAD6)
val md_light_onErrorContainer = Color(0xFF410002)
val md_light_background = Color(0xFFFFF8F5)
val md_light_onBackground = Color(0xFF221A16)
val md_light_surface = Color(0xFFFFF8F5)
val md_light_onSurface = Color(0xFF221A16)
val md_light_surfaceVariant = Color(0xFFF4DED4)
val md_light_onSurfaceVariant = Color(0xFF53433C)
val md_light_outline = Color(0xFF85736B)
val md_light_outlineVariant = Color(0xFFD7C2B9)
val md_light_surfaceContainer = Color(0xFFFCECE5)
val md_light_surfaceContainerHigh = Color(0xFFF7E6DF)
val md_light_inverseSurface = Color(0xFF382E2A)
val md_light_inverseOnSurface = Color(0xFFFFEDE7)
val md_light_scrim = Color(0xFF000000)

// Dark
val md_dark_primary = Color(0xFFFFB693)
val md_dark_onPrimary = Color(0xFF552100)
val md_dark_primaryContainer = Color(0xFF733314)
val md_dark_onPrimaryContainer = Color(0xFFFFDBCB)
val md_dark_secondary = Color(0xFFB9CCB4)
val md_dark_onSecondary = Color(0xFF253423)
val md_dark_secondaryContainer = Color(0xFF3B4B38)
val md_dark_onSecondaryContainer = Color(0xFFD5E8CF)
val md_dark_tertiary = Color(0xFFEFC04E)
val md_dark_onTertiary = Color(0xFF402D00)
val md_dark_tertiaryContainer = Color(0xFF5C4300)
val md_dark_onTertiaryContainer = Color(0xFFFFDF9B)
val md_dark_error = Color(0xFFFFB4AB)
val md_dark_onError = Color(0xFF690005)
val md_dark_errorContainer = Color(0xFF93000A)
val md_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_dark_background = Color(0xFF1A120E)
val md_dark_onBackground = Color(0xFFF1DFD8)
val md_dark_surface = Color(0xFF1A120E)
val md_dark_onSurface = Color(0xFFF1DFD8)
val md_dark_surfaceVariant = Color(0xFF53433C)
val md_dark_onSurfaceVariant = Color(0xFFD7C2B9)
val md_dark_outline = Color(0xFF9F8D85)
val md_dark_outlineVariant = Color(0xFF53433C)
val md_dark_surfaceContainer = Color(0xFF271E19)
val md_dark_surfaceContainerHigh = Color(0xFF322823)
val md_dark_inverseSurface = Color(0xFFF1DFD8)
val md_dark_inverseOnSurface = Color(0xFF382E2A)
val md_dark_scrim = Color(0xFF000000)

/** Cores semânticas da app (fora do colorScheme do M3). */
data class AppColors(
    val overdue: Color,
    val done: Color,
    val eventDot: Color,
    val weatherWarm: Color,
    val weatherCold: Color,
)

val LightAppColors = AppColors(
    overdue = Color(0xFFB3261E),
    done = Color(0xFF3E6B46),
    eventDot = md_light_tertiary,
    weatherWarm = Color(0xFFD97706),
    weatherCold = Color(0xFF3E6B8A),
)

val DarkAppColors = AppColors(
    overdue = Color(0xFFFFB4AB),
    done = Color(0xFF9FCFA6),
    eventDot = md_dark_tertiary,
    weatherWarm = Color(0xFFF0B24A),
    weatherCold = Color(0xFF8FBEDB),
)
```

### Cores de peças de roupa (etiquetas, não tema)

Chaves estáveis guardadas na BD; o rótulo é traduzido e a amostra é fixa:
`BLACK #1C1B1F` · `WHITE #FFFFFF` · `GREY #9E9E9E` · `BEIGE #D8C3A5` · `BROWN #7B5E43` ·
`RED #C0392B` · `ORANGE #E07A2F` · `YELLOW #E1B12C` · `GREEN #4E7D52` · `BLUE #34618E` ·
`NAVY #22314E` · `PURPLE #6B4E8A` · `PINK #D98BA5` · `DENIM #4A6A8A` · `MULTI` (gradiente).
Neutras (preto, branco, cinzento, bege, castanho, navy) combinam com tudo — usado pelo gerador.

## 3. Tipografia

- **Títulos:** *Outfit* (Google Fonts) — geométrica, amigável, e o nome cai a calhar.
- **Corpo e UI:** *Inter*.
- Fallback: tipo de letra do sistema. Empacotar as fontes em `res/font/` (não depender de download).

### `ui/theme/Type.kt`

```kotlin
package com.dailycompany.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dailycompany.R

val Display = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
)
val Body = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

val AppTypography = Typography(
    displaySmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Body, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Body, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Body, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)
```

Uso: saudação/data → `headlineMedium` · títulos de ecrã → `titleLarge` · títulos de secção → `titleMedium` · nome de peça no cartão → `bodyMedium` (máx. 2 linhas) · metadados e chips → `labelMedium`.

## 4. Espaço, forma, elevação

### `ui/theme/Dimens.kt`

```kotlin
package com.dailycompany.ui.theme

import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val screenH = 16.dp      // margem horizontal do ecrã
    val gridGap = 10.dp      // espaço entre cartões da grelha
    val touchMin = 48.dp
}

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),   // cartões
    large = RoundedCornerShape(22.dp),    // cartão do outfit, bottom sheets
    extraLarge = RoundedCornerShape(28.dp),
)
```

- Grelha de 4 dp. Nada de números mágicos nos ecrãs.
- Elevação: **cor antes de sombra** (`surfaceContainer`/`surfaceContainerHigh`). Sombra só no FAB e em bottom sheets.
- Rácio das fotos de peças: **3:4** (retrato) nas grelhas; **1:1** nas miniaturas de outfit.

## 5. `ui/theme/Theme.kt`

```kotlin
package com.dailycompany.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LightScheme = lightColorScheme(
    primary = md_light_primary, onPrimary = md_light_onPrimary,
    primaryContainer = md_light_primaryContainer, onPrimaryContainer = md_light_onPrimaryContainer,
    secondary = md_light_secondary, onSecondary = md_light_onSecondary,
    secondaryContainer = md_light_secondaryContainer, onSecondaryContainer = md_light_onSecondaryContainer,
    tertiary = md_light_tertiary, onTertiary = md_light_onTertiary,
    tertiaryContainer = md_light_tertiaryContainer, onTertiaryContainer = md_light_onTertiaryContainer,
    error = md_light_error, onError = md_light_onError,
    errorContainer = md_light_errorContainer, onErrorContainer = md_light_onErrorContainer,
    background = md_light_background, onBackground = md_light_onBackground,
    surface = md_light_surface, onSurface = md_light_onSurface,
    surfaceVariant = md_light_surfaceVariant, onSurfaceVariant = md_light_onSurfaceVariant,
    outline = md_light_outline, outlineVariant = md_light_outlineVariant,
    surfaceContainer = md_light_surfaceContainer, surfaceContainerHigh = md_light_surfaceContainerHigh,
    inverseSurface = md_light_inverseSurface, inverseOnSurface = md_light_inverseOnSurface,
    scrim = md_light_scrim,
)

private val DarkScheme = darkColorScheme(
    primary = md_dark_primary, onPrimary = md_dark_onPrimary,
    primaryContainer = md_dark_primaryContainer, onPrimaryContainer = md_dark_onPrimaryContainer,
    secondary = md_dark_secondary, onSecondary = md_dark_onSecondary,
    secondaryContainer = md_dark_secondaryContainer, onSecondaryContainer = md_dark_onSecondaryContainer,
    tertiary = md_dark_tertiary, onTertiary = md_dark_onTertiary,
    tertiaryContainer = md_dark_tertiaryContainer, onTertiaryContainer = md_dark_onTertiaryContainer,
    error = md_dark_error, onError = md_dark_onError,
    errorContainer = md_dark_errorContainer, onErrorContainer = md_dark_onErrorContainer,
    background = md_dark_background, onBackground = md_dark_onBackground,
    surface = md_dark_surface, onSurface = md_dark_onSurface,
    surfaceVariant = md_dark_surfaceVariant, onSurfaceVariant = md_dark_onSurfaceVariant,
    outline = md_dark_outline, outlineVariant = md_dark_outlineVariant,
    surfaceContainer = md_dark_surfaceContainer, surfaceContainerHigh = md_dark_surfaceContainerHigh,
    inverseSurface = md_dark_inverseSurface, inverseOnSurface = md_dark_inverseOnSurface,
    scrim = md_dark_scrim,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
val LocalSpacing = staticCompositionLocalOf { Spacing }

@Composable
fun DailyCompanyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    CompositionLocalProvider(LocalAppColors provides appColors, LocalSpacing provides Spacing) {
        MaterialTheme(colorScheme = scheme, typography = AppTypography, shapes = AppShapes, content = content)
    }
}
```

`enableEdgeToEdge()` na Activity; as barras de sistema seguem o tema. A preferência claro/escuro/sistema vem do DataStore e é passada a `darkTheme`.

## 6. Ícones e ilustração

- `Icons.Rounded.*` (cantos arredondados combinam com as formas). Evitar `material-icons-extended` inteiro.
- Ícones-chave: Hoje `WbSunny` · Tarefas `CheckCircleOutline` · Agenda `CalendarMonth` · Guarda-roupa `Checkroom` · Outfits `AutoAwesome` · Aleatório **dado** (vetor próprio) · Cadeado `Lock`/`LockOpen` · Na lavagem `LocalLaundryService`.
- Estados vazios: ilustração simples em linha, uma cor + `onSurfaceVariant`, nunca mascotes nem 3D.
- Ícone da app: cabide/sol estilizado sobre terracota; versão monocromática e adaptativa obrigatórias.

## 7. Componentes partilhados (`ui/components/`)

Todos stateless, com `modifier`, preview em claro/escuro, e criados **antes** de duplicar UI:

`ItemImage` · `ClothingCard` · `OutfitCollage` · `SlotCarousel` · `LockToggle` · `SizePicker` · `SizeChip` · `CategoryPicker` · `ColorDot` · `FilterChipRow` · `SectionHeader` · `TaskRow` · `EventRow` · `DateTimeField` · `RecurrencePicker` · `ReminderPicker` · `NameInputDialog` · `ConfirmDialog` · `EmptyState` · `ErrorState` · `LoadingState` · `UndoSnackbarHost` · `DiceButton`.

Regras de cartão: imagem em cima (3:4), nome 1–2 linhas com reticências, badges pequenos (tamanho, favorito, na lavagem) sobrepostos em cima da imagem com fundo `scrim` a 40%.

## 8. Estados

Cada ecrã com dados modela **Loading · Content · Empty · Error(retry)**.
- Loading: cartões-fantasma (alfa animada) nas grelhas; spinner só para esperas curtas.
- Empty: ícone + uma frase + botão. Exemplos: *"Ainda não tens peças. Adiciona a primeira."* · *"Dia livre."* · *"Sem outfits guardados. Experimenta o botão dos dados."*
- Error: frase humana + "Tentar novamente". Nunca `Exception` em bruto.
- Sem candidatos no gerador: diz exatamente o que falta ("Faltam calçado disponível").

## 9. Movimento

- Transições de ecrã: `fadeIn + slideIn` horizontal curto (≤ 250 ms).
- Lista: `Modifier.animateItem()` ao inserir/remover; `animateContentSize` ao expandir.
- Botão dos dados: rotação do ícone (~400 ms) + `Crossfade` das peças + `HapticFeedbackType.LongPress`.
- Concluir tarefa: risco a aparecer + fade para `onSurfaceVariant` antes de sair da lista.
- Nada acima de ~300 ms (exceto o dado); respeitar "remover animações" do sistema.

## 10. Acessibilidade (obrigatório)

- Alvos ≥ 48 dp; `contentDescription` em imagens com significado (`null` nas decorativas); botões só com ícone têm rótulo.
- Texto até **200%** de escala sem cortes: sem alturas fixas em contentores de texto, `maxLines` + `overflow`.
- Contraste vindo do tema; estado nunca só por cor (ex.: "na lavagem" = ícone + texto, não só opacidade).
- Cadeados e interruptores expõem estado por `semantics`.
- TalkBack testado nos fluxos: adicionar peça, gerar outfit, concluir tarefa.

## 11. Tom de voz (resumo — detalhe em `android-localization-pt-pt`)

PT-PT, tratamento por **tu**, curto e direto. Botões de uma ou duas palavras: *Guardar · Cancelar · Anular · Adicionar · Editar · Apagar · Outro · Usar hoje · Tentar novamente*. Erros dizem o que aconteceu e o que fazer. Nada de exclamações a mais nem de linguagem técnica.

## 12. Checklist de UI (antes de fechar qualquer ecrã)

- [ ] Só tokens do tema (zero cores/tamanhos hardcoded).
- [ ] Claro e escuro verificados em preview (`@PreviewLightDark`).
- [ ] 200% de escala de texto sem cortes (`@PreviewFontScale`).
- [ ] Loading / Content / Empty / Error implementados.
- [ ] Insets e teclado tratados; nada escondido por baixo das barras.
- [ ] Uma ação primária clara, ao alcance do polegar.
- [ ] Componentes reutilizados de `ui/components/`.
- [ ] Textos em `strings.xml` (PT-PT, com plurais).
- [ ] Apagar tem Anular.
- [ ] `key` estável nas listas; grelhas só com thumbnails.
