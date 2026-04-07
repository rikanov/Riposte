package hu.riposte.game

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import hu.riposte.game.R // Biztosítjuk, hogy a mi strings.xml-ünket húzza be

// --- ALAP BEÁLLÍTÁSOK ---
data class AppSettings(
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val riposteEnabled: Boolean = true,
    val themeId: String = "abstract_sunrise"
)

private val DialogContentColor = Color.White
private val DialogDarkTextColor = Color(0xFF1E272E)

// JAVÍTVA: Enum most már String Erőforrás ID-t tárol
enum class AiDifficulty(val level: Int, @StringRes val titleRes: Int) {
    BEGINNER(5, R.string.diff_beginner),
    NORMAL(7, R.string.diff_normal),
    EXPERT(9, R.string.diff_expert),
    PRO(10, R.string.diff_pro)
}

// --- AZ UNIVERZÁLIS "JUICY GLASS" KONTÉNER ---
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    val accentColor = LocalGameTheme.current.uiAccentColor

    val shimmerProgress = rememberInfiniteTransition(label = "dialog_shimmer").animateFloat(
        initialValue = -0.5f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer_anim"
    )

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.55f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch { alpha.animateTo(1f, tween(200)) }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                }
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E272E).copy(alpha = 0.85f))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .drawWithContent {
                    drawContent()
                    val w = size.width
                    val h = size.height
                    val xOffset = w * shimmerProgress.value
                    val shimmerBrush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.8f), Color.Transparent),
                        start = Offset(xOffset, 0f),
                        end = Offset(xOffset + 200f, h)
                    )
                    drawRoundRect(
                        brush = shimmerBrush,
                        size = size,
                        cornerRadius = CornerRadius(24.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                .padding(24.dp)
        ) {
            content()
        }
    }
}

// --- AZ UNIVERZÁLIS LEGÖRDÜLŐ MENÜ (GLASS DROPDOWN) ---
@Composable
fun <T> GlassDropdown(
    label: String,
    items: List<T>,
    selectedItem: T,
    itemLabel: @Composable (T) -> String, // JAVÍTVA: Composable, hogy fogadja a stringResource-t
    onItemSelected: (T) -> Unit,
    soundManager: SoundManager
) {
    var expanded by remember { mutableStateOf(false) }
    val accentColor = LocalGameTheme.current.uiAccentColor

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(
            text = label.uppercase(),
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .clickable {
                    soundManager.playClick()
                    expanded = true
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = itemLabel(selectedItem),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = DialogContentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF2F3640))
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = itemLabel(item), color = Color.White, fontSize = 14.sp) },
                        onClick = {
                            soundManager.playClick()
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// --- A FŐMENÜ KONTÉNER ÉS A NAVIGÁCIÓ ---
@Composable
fun MainMenuDialog(
    soundManager: SoundManager,
    appSettings: AppSettings,
    onStart: (GameSettings) -> Unit,
    onCancel: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(MenuScreen.MAIN) }
    val accentColor = LocalGameTheme.current.uiAccentColor

    GlassDialog(onDismissRequest = onCancel) {
        Column(modifier = Modifier.width(280.dp)) {

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    if (targetState != MenuScreen.MAIN && initialState == MenuScreen.MAIN) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "MenuTransition"
            ) { targetScreen ->
                when (targetScreen) {

                    MenuScreen.MAIN -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                stringResource(R.string.menu_new_match),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor,
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            MenuButton(text = stringResource(R.string.menu_player_vs_ai), isPrimary = true, onClick = { soundManager.playClick(); currentScreen = MenuScreen.VS_AI_SETTINGS })
                            Spacer(modifier = Modifier.height(12.dp))
                            MenuButton(
                                text = stringResource(R.string.menu_2_local_players),
                                onClick = {
                                    soundManager.playClick()
                                    onStart(GameSettings(GameMode.LOCAL_MULTIPLAYER, StartingPlayer.ALTERNATING, 5, appSettings.riposteEnabled))
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            MenuButton(text = stringResource(R.string.menu_how_to_play), onClick = { soundManager.playClick() })
                        }
                    }

                    MenuScreen.VS_AI_SETTINGS -> {
                        var difficulty by remember { mutableStateOf(AiDifficulty.NORMAL) }
                        var starter by remember { mutableStateOf(StartingPlayer.PLAYER) }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(bottom = 20.dp)
                                    .clickable { soundManager.playClick(); currentScreen = MenuScreen.MAIN }
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.menu_vs_ai_settings), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
                            }

                            GlassDropdown(
                                label = stringResource(R.string.menu_ai_difficulty),
                                items = AiDifficulty.entries,
                                selectedItem = difficulty,
                                itemLabel = { stringResource(id = it.titleRes) },
                                onItemSelected = { difficulty = it },
                                soundManager = soundManager
                            )

                            GlassDropdown(
                                label = stringResource(R.string.menu_first_move),
                                items = StartingPlayer.entries,
                                selectedItem = starter,
                                itemLabel = {
                                    when(it.name) {
                                        "AI" -> stringResource(R.string.starter_ai)
                                        "PLAYER" -> stringResource(R.string.starter_player)
                                        "ALTERNATING" -> stringResource(R.string.starter_alternating)
                                        else -> it.name
                                    }
                                },
                                onItemSelected = { starter = it },
                                soundManager = soundManager
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    soundManager.playClick()
                                    onStart(GameSettings(GameMode.VS_AI, starter, difficulty.level, appSettings.riposteEnabled))
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                            ) {
                                Text(stringResource(R.string.menu_start_match), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = DialogDarkTextColor)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

// --- OPTIONS (BEÁLLÍTÁSOK) ABLAK ---
@Composable
fun OptionsDialog(
    soundManager: SoundManager,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onClose: () -> Unit
) {
    val accentColor = LocalGameTheme.current.uiAccentColor

    GlassDialog(onDismissRequest = onClose) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.options_title), color = accentColor, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(32.dp))

            SettingsToggleRow(stringResource(R.string.options_music), settings.musicEnabled) {
                soundManager.playToggle(it)
                onSettingsChange(settings.copy(musicEnabled = it))
            }
            Spacer(modifier = Modifier.height(8.dp))
            SettingsToggleRow(stringResource(R.string.options_sfx), settings.sfxEnabled) {
                soundManager.playToggle(it)
                onSettingsChange(settings.copy(sfxEnabled = it))
            }
            Spacer(modifier = Modifier.height(8.dp))
            SettingsToggleRow(stringResource(R.string.options_haptic), settings.hapticEnabled) {
                soundManager.playToggle(it)
                onSettingsChange(settings.copy(hapticEnabled = it))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            SettingsToggleRow(stringResource(R.string.options_riposte), settings.riposteEnabled) {
                soundManager.playToggle(it)
                onSettingsChange(settings.copy(riposteEnabled = it))
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    soundManager.playClick()
                    onClose()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                Text(stringResource(R.string.options_done), fontWeight = FontWeight.Bold, color = DialogDarkTextColor)
            }
        }
    }
}

@Composable
fun GameOverDialog(
    soundManager: SoundManager,
    winnerName: String,
    onRestart: () -> Unit
) {
    // Megj: Később a GameViewModel-ben a "winner" logikát is le lehet cserélni ID-kra,
    // de egyelőre a string tartalmára keresünk.
    val isWin = winnerName.contains("You", ignoreCase = true) || winnerName.contains("Player", ignoreCase = true)

    val mainColor = if (isWin) Color(0xFFFFD700) else Color(0xFF64B5F6)
    val titleText = if (isWin) stringResource(R.string.gameover_victory) else stringResource(R.string.gameover_defeat)
    val subText = if (isWin) stringResource(R.string.gameover_you_won) else stringResource(R.string.gameover_ai_won)

    val bgPainter = if (isWin) {
        painterResource(id = R.drawable.ic_victory_bg)
    } else {
        painterResource(id = R.drawable.ic_defeat_bg)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "DialogPulse")
    val bgScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BgScaleAnim"
    )
    Dialog(
        onDismissRequest = { /* Nem engedjük bezárni mellékattintással */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E272E).copy(alpha = 0.85f))
                .border(2.dp, mainColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = bgPainter,
                contentDescription = stringResource(R.string.cd_bg_mood_icon),
                tint = mainColor.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(180.dp)
                    .graphicsLayer {
                        scaleX = bgScale
                        scaleY = bgScale
                    }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = titleText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = mainColor,
                    letterSpacing = 4.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = subText,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Button(
                    onClick = {
                        soundManager.playClick()
                        onRestart()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mainColor,
                        contentColor = Color(0xFF0F141E)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.gameover_new_game),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PauseMenuDialog(
    soundManager: SoundManager,
    onResume: () -> Unit,
    onNewGame: () -> Unit,
    onOptions: () -> Unit,
    onUndo: () -> Unit,
    onExit: () -> Unit
) {
    val accentColor = LocalGameTheme.current.uiAccentColor

    GlassDialog(onDismissRequest = onResume) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.pause_title), color = accentColor, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(32.dp))

            MenuButton(text = stringResource(R.string.pause_resume), onClick = { soundManager.playClick(); onResume() }, isPrimary = true)
            Spacer(modifier = Modifier.height(16.dp))
            MenuButton(text = stringResource(R.string.pause_new_game), onClick = { soundManager.playClick(); onNewGame() })
            Spacer(modifier = Modifier.height(16.dp))
            MenuButton(text = stringResource(R.string.pause_options), onClick = { soundManager.playClick(); onOptions() })
            Spacer(modifier = Modifier.height(16.dp))
            MenuButton(text = stringResource(R.string.pause_undo), onClick = { soundManager.playClick(); onUndo() })
            Spacer(modifier = Modifier.height(32.dp))
            MenuButton(text = stringResource(R.string.pause_exit), onClick = { soundManager.playClick(); onExit() }, isDanger = true)
        }
    }
}

@Composable
private fun SettingsToggleRow(text: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val accentColor = LocalGameTheme.current.uiAccentColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!isChecked) }
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, color = DialogContentColor, fontWeight = FontWeight.Medium)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit, isPrimary: Boolean = false, isDanger: Boolean = false) {
    val accentColor = LocalGameTheme.current.uiAccentColor

    val bgColor = when {
        isPrimary -> accentColor
        isDanger -> Color(0xFFE53935).copy(alpha = 0.15f)
        else -> Color.Black.copy(alpha = 0.3f)
    }
    val textColor = when {
        isPrimary -> DialogDarkTextColor
        isDanger -> Color(0xFFEF5350)
        else -> DialogContentColor
    }
    val borderColor = when {
        isPrimary -> Color.Transparent
        isDanger -> Color(0xFFE53935).copy(alpha = 0.5f)
        else -> Color.White.copy(alpha = 0.15f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, fontSize = 14.sp)
    }
}

data class Particle(val vx: Float, val vy: Float, val color: Color, val size: Float)

@Composable
fun FireworksOverlay() {
    val particles = remember {
        List(150) {
            Particle(
                vx = (Math.random() * 60 - 30).toFloat(),
                vy = (Math.random() * -60 - 20).toFloat(),
                color = Color(
                    red = (100..255).random() / 255f,
                    green = (100..255).random() / 255f,
                    blue = (100..255).random() / 255f,
                    alpha = 1f
                ),
                size = (Math.random() * 12 + 6).toFloat()
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2500, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 1f - (progress.value * 0.5f) }) {
        val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.35f)

        particles.forEach { p ->
            val currentX = centerOffset.x + (p.vx * progress.value * 50)
            val currentY = centerOffset.y + (p.vy * progress.value * 50 + 60f * progress.value * progress.value * 50)

            drawCircle(
                color = p.color.copy(alpha = 1f - progress.value),
                radius = p.size * (1f - progress.value),
                center = androidx.compose.ui.geometry.Offset(currentX, currentY)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemeSelectorDialog(
    currentThemeId: String,
    onThemeSelected: (String) -> Unit,
    onThemePreview: (String) -> Unit,
    onDismiss: () -> Unit,
    soundManager: SoundManager
) {
    val themes = ThemeRegistry.allThemes
    val initialPage = themes.indexOfFirst { it.id == currentThemeId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { themes.size }
    val accentColor = LocalGameTheme.current.uiAccentColor

    LaunchedEffect(pagerState.currentPage) {
        val selectedTheme = themes[pagerState.currentPage]
        onThemePreview(selectedTheme.id)
    }

    GlassDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.width(300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.theme_title),
                color = accentColor,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .height(280.dp)
                    .fillMaxWidth(),
                pageSpacing = 32.dp
            ) { page ->
                val theme = themes[page]
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ThemeCard(theme = theme, isSelected = pagerState.currentPage == page)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = themes[pagerState.currentPage].displayName.uppercase(),
                color = DialogContentColor,
                letterSpacing = 2.sp,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accentColor)
                        .clickable {
                            soundManager.playClick()
                            onThemeSelected(themes[pagerState.currentPage].id)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.theme_apply), fontWeight = FontWeight.Bold, color = DialogDarkTextColor)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable {
                            soundManager.playClick()
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.theme_cancel), fontWeight = FontWeight.Bold, color = DialogContentColor)
                }
            }
        }
    }
}

@Composable
fun ThemeCard(theme: GameTheme, isSelected: Boolean) {
    val scale by animateFloatAsState(if (isSelected) 1f else 0.85f, label = "card_scale")
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.5f, label = "card_alpha")

    Card(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(5f / 7f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(3.dp, if (theme.id == "random") Color.Yellow else theme.auraP1Color) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (theme.id == "random") Color(0xFF1E272E) else Color.Transparent)
        ) {
            Image(
                painter = painterResource(id = theme.previewImageRes),
                contentDescription = theme.displayName,
                contentScale = if (theme.id == "random") ContentScale.Fit else ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (theme.id == "random") 32.dp else 0.dp)
            )
        }
    }
}