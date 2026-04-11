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
import androidx.compose.material.icons.rounded.TouchApp
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

import hu.riposte.game.R
// --- BASE SETTINGS ---
data class AppSettings(
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val visualAssistsEnabled: Boolean = true,
    val nightModeEnabled: Boolean = false,
    val riposteEnabled: Boolean = true,
    val themeId: String = "abstract_sunrise",
    val hasSeenTutorial: Boolean = false
)

private val DialogContentColor = Color.White
private val DialogDarkTextColor = Color(0xFF1E272E)

enum class AiDifficulty(val level: Int, @StringRes val titleRes: Int) {
    BEGINNER(5, R.string.diff_beginner),
    NORMAL(7, R.string.diff_normal),
    EXPERT(9, R.string.diff_expert),
    PRO(10, R.string.diff_pro)
}

// --- UNIVERSAL "JUICY GLASS" CONTAINER ---
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
    onStartTutorial: () -> Unit, // <--- ÚJ CALLBACK a tutorial indításához
    onCancel: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(MenuScreen.MAIN) }
    val accentColor = LocalGameTheme.current.uiAccentColor

    GlassDialog(onDismissRequest = onCancel) {
        Column(modifier = Modifier.width(280.dp)) {

            AnimatedContent(
                targetState = currentScreen,
                // ... a meglévő transitionSpec logikád marad ...
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
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
                            // JAVÍTÁS: Itt hívjuk meg az új tutorial indítót!
                            MenuButton(text = stringResource(R.string.menu_how_to_play), onClick = { soundManager.playClick(); onStartTutorial() })
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
@Composable
fun TutorialWelcomeDialog(
    soundManager: SoundManager,
    onDismiss: () -> Unit
) {
    val accentColor = LocalGameTheme.current.uiAccentColor

    // --- ÚJ: Fázis kezelés ---
    var currentPhase by remember { mutableIntStateOf(1) } // 1: Swipe, 2: Hold & Select

    val infiniteTransition = rememberInfiniteTransition(label = "TutAnim")

    // Animációk az 1. Fázishoz (Gyors Swipe)
    val swipeX1 by infiniteTransition.animateFloat(
        initialValue = -60f, targetValue = 60f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing, delayMillis = 600), RepeatMode.Restart), label = "s1"
    )
    val touchScale1 by infiniteTransition.animateFloat(
        initialValue = 1.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            keyframes { durationMillis = 1400; 1.3f at 0; 0.9f at 300; 0.9f at 1100; 1.3f at 1400 }, RepeatMode.Restart
        ), label = "ts1"
    )

    // Animációk a 2. Fázishoz (Lassú Hold & Select)
    val swipeX2 by infiniteTransition.animateFloat(
        initialValue = -60f, targetValue = 60f,
        // Sokkal lassabb, "húzós" mozdulat
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearOutSlowInEasing, delayMillis = 600), RepeatMode.Restart), label = "s2"
    )
    val touchScale2 by infiniteTransition.animateFloat(
        initialValue = 1.3f, targetValue = 0.9f,
        // Sokáig lent marad az ujj
        animationSpec = infiniteRepeatable(
            keyframes { durationMillis = 3100; 1.3f at 0; 0.9f at 300; 0.9f at 2800; 1.3f at 3100 }, RepeatMode.Restart
        ), label = "ts2"
    )

    GlassDialog(onDismissRequest = { }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    soundManager.playClick()
                    if (currentPhase == 1) {
                        currentPhase = 2 // Első kattintásra átvált a "lövész" módra
                    } else {
                        onDismiss() // Második kattintásra bezár és indul a játék
                    }
                }
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (currentPhase == 1) "HOW TO MOVE" else "PRO TIP: AIMING",
                color = accentColor, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(32.dp))

            // --- AZ ANIMÁLT VIZUÁLIS BEMUTATÓ ---
            Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {

                // Aktuális fázis animációinak kiválasztása
                val currentSwipeX = if (currentPhase == 1) swipeX1 else swipeX2
                val currentTouchScale = if (currentPhase == 1) touchScale1 else touchScale2
                val currentTouchAlpha = if (currentTouchScale <= 1.0f) 1f else 0f

                if (currentPhase == 1) {
                    // 1. Fázis: Elmosódó csík (Trail)
                    Box(modifier = Modifier.width(100.dp).height(40.dp).background(
                        Brush.horizontalGradient(colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.3f), accentColor.copy(alpha = 0.8f))),
                        shape = RoundedCornerShape(20.dp)
                    ).graphicsLayer { alpha = currentTouchAlpha * 0.8f }
                    )
                } else {
                    // 2. Fázis: "Ghost Piece" és Vonal imitálása (Ahogy kérted!)
                    if (currentTouchAlpha > 0f) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(color = accentColor, start = Offset(0f, size.height/2), end = Offset(size.width, size.height/2), strokeWidth = 4.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f))
                        }
                        // Szellembábu a falnál
                        Image(painter = painterResource(id = LocalGameTheme.current.pieceP1Res), contentDescription = null, modifier = Modifier.size(40.dp).offset(x = 40.dp).graphicsLayer { alpha = 0.4f })
                    }
                }

                // A toló Bábu
                val pieceOffset = if (currentTouchScale <= 0.9f) currentSwipeX.coerceAtLeast(-40f) else -40f
                Image(
                    painter = painterResource(id = LocalGameTheme.current.pieceP1Res), contentDescription = "Tutorial Piece",
                    modifier = Modifier.size(40.dp).offset(x = pieceOffset.dp)
                )

                // Az Ujj
                Icon(
                    imageVector = Icons.Rounded.TouchApp, contentDescription = "Swipe", tint = Color.White,
                    modifier = Modifier.size(56.dp).offset(x = currentSwipeX.dp, y = 20.dp).graphicsLayer { scaleX = currentTouchScale; scaleY = currentTouchScale; alpha = currentTouchAlpha }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = if (currentPhase == 1) "Swipe any piece to slide it.\nIt will only stop when it hits a wall or another piece."
                else "Hold your finger on the screen and move it slowly to select your target visually before releasing.",
                color = DialogContentColor, textAlign = TextAlign.Center, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
            val infinitePulse = rememberInfiniteTransition()
            val textAlpha by infinitePulse.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "")

            Text(
                text = if (currentPhase == 1) "- TAP TO SEE PRO TIP -" else "- TAP TO PLAY -",
                color = accentColor.copy(alpha = textAlpha), fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp
            )
        }
    }
}
@Composable
fun TutorialCompleteDialog(
    soundManager: SoundManager,
    onBackToMenu: () -> Unit // Most már a Menübe térünk vissza!
) {
    val accentColor = LocalGameTheme.current.uiAccentColor

    // Villogó szöveg animációja a "TAP TO CONTINUE"-hoz
    val infinitePulse = rememberInfiniteTransition()
    val textAlpha by infinitePulse.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "PulseAnim"
    )

    GlassDialog(onDismissRequest = { /* Csak a belső felületre kattintva zárható be */ }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    soundManager.playClick()
                    onBackToMenu()
                } // Bárhova kattintva kilép a menübe
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Text("TUTORIAL COMPLETED", color = accentColor, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(16.dp))

            Text(
                "Great job! You now know all the rules.\nYou are ready to test your skills in the real matches!",
                color = Color.White, textAlign = TextAlign.Center, fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            // Villogó "TAP TO EXIT" instrukció
            Text(
                text = "- TAP TO RETURN TO MENU -",
                color = accentColor.copy(alpha = textAlpha),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }
    }
}