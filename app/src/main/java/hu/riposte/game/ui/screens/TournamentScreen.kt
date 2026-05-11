package hu.riposte.game.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import hu.riposte.game.R
import hu.riposte.game.engine.logic.GameViewModel
import hu.riposte.game.engine.logic.SettingsManager
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.engine.data.TournamentOpponent
import hu.riposte.game.engine.data.TournamentRoster
import hu.riposte.game.engine.utils.rememberDeviceTilt
import hu.riposte.game.ui.components.RiposteSystemButton
import hu.riposte.game.ui.components.VolumetricLightOrgan // <-- ÚJ IMPORT
import hu.riposte.game.ui.dialogs.OpponentCardOverlay
import hu.riposte.game.ui.dialogs.PlayerStatsOverlay
import hu.riposte.game.ui.dialogs.PremiumUnlockDialog
import hu.riposte.game.ui.theme.LocalGameTheme
import hu.riposte.game.ui.theme.ThemeRegistry
import kotlinx.coroutines.launch
import kotlin.math.max

// --- AMBIENT DUST VFX ---
@Composable
fun AmbientDustVFX(accentColor: Color = Color(0xFFD4AF37)) {
    val particles = remember {
        List(40) {
            object {
                val x = (Math.random() * 1000f).toFloat()
                val y = (Math.random() * 2000f).toFloat()
                val speed = (Math.random() * 0.5f + 0.2f).toFloat()
                val size = (Math.random() * 4f + 2f).toFloat()
                val alpha = (Math.random() * 0.4f + 0.1f).toFloat()
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_vfx")
    val moveAnim by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "ambient_move"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val currentY = (p.y - moveAnim * p.speed) % size.height
            val drawY = if (currentY < 0) currentY + size.height else currentY

            drawCircle(
                color = accentColor.copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(p.x % size.width, drawY)
            )
        }
    }
}

// --- MAIN SCREEN ---
@Composable
fun TournamentScreen(
    gameViewModel: GameViewModel,
    soundManager: SoundManager,
    onBackToMenu: () -> Unit,
    onStartMatch: () -> Unit,
    onResumeGame: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsManager = remember { SettingsManager(context) }
    val appSettings by settingsManager.settingsFlow.collectAsState(initial = null)

    val manager = gameViewModel.tournamentManager
    val density = LocalDensity.current.density
    val deviceTilt by rememberDeviceTilt()
    val listState = rememberLazyListState()

    var opponentToShow by remember { mutableStateOf<TournamentOpponent?>(null) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var showPlayerStats by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "SyncedVFX")
    val syncedPulse by infiniteTransition.animateFloat(
        initialValue = 0.01f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(tween(4000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "Pulse"
    )

    LaunchedEffect(appSettings) {
        appSettings?.let { settings ->
            soundManager.isMusicEnabled = settings.musicEnabled
            if (settings.musicEnabled) soundManager.playThemeMusic(R.raw.main_menu_music)

            manager.loadState(
                rank = settings.tournamentRank,
                highest = settings.tournamentHighest,
                defending = settings.tournamentDefending,
                historyString = settings.tournamentMatchHistory,
                highestRatingScore = settings.highestRating
            )
            val scrollToIndex = max(0, manager.currentRank - 4)
            listState.animateScrollToItem(scrollToIndex)
        }
    }

    val activeTheme = ThemeRegistry.Tournament
    val hasOngoingMatch = appSettings?.hasSavedTournamentMatch == true

    CompositionLocalProvider(LocalGameTheme provides activeTheme) {
        val accentColor = LocalGameTheme.current.uiAccentColor

        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.bg_hall),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    scaleX = 1.15f; scaleY = 1.15f
                    translationX = (deviceTilt.x * 6f * density).coerceIn(-40f, 40f)
                    translationY = (-deviceTilt.y * 6f * density).coerceIn(-40f, 40f)
                }
            )

            AmbientDustVFX(accentColor = accentColor)

            Column(
                modifier = Modifier.fillMaxSize().padding(top = 16.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- 2. HALL OF LEGENDS LOGO KOMPOZÍCIÓ ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth(1.0f)
                        .height(180.dp)
                        .offset(y = (-45).dp)
                        .zIndex(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // 1. Glow Layer
                    Image(
                        painter = painterResource(id = R.drawable.logo_hall),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize(0.88f)
                            .graphicsLayer {
                                translationX = (-deviceTilt.x * 2.5f * density).coerceIn(-12f, 12f)
                                translationY = (deviceTilt.y * 2.5f * density).coerceIn(-12f, 12f)
                                alpha = 0.6f + (syncedPulse * 4f)
                            },
                        colorFilter = ColorFilter.tint(accentColor)
                    )

                    // 2. Shadow layer
                    Image(
                        painter = painterResource(id = R.drawable.logo_hall),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize(1.0f)
                            .graphicsLayer {
                                translationX = (-deviceTilt.x * 3f * density).coerceIn(-15f, 15f) + 4f
                                translationY = (deviceTilt.y * 3f * density).coerceIn(-15f, 15f) + 4f
                                alpha = 0.5f
                            },
                        colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.5f))
                    )

                    // 3. Fő Logó
                    Image(
                        painter = painterResource(id = R.drawable.logo_hall),
                        contentDescription = stringResource(id = R.string.tournament_title_main),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize(1.0f)
                            .graphicsLayer {
                                translationX = (-deviceTilt.x * 3f * density).coerceIn(-15f, 15f)
                                translationY = (deviceTilt.y * 3f * density).coerceIn(-15f, 15f)
                            },
                    )

                    // --- 4. A FÉNYORGONA MEGHÍVÁSA KISZERVEZVE ---
                    VolumetricLightOrgan(
                        accentColor = accentColor,
                        centerYRatio = 0.6f // A logó esetében kicsit lentebbről indul
                    )
                }

                // --- 3. TOURNAMENT LISTA ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.66f)
                        .weight(1f)
                        .offset(y = (-40).dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF0F141E).copy(alpha = 0.65f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(20) { index ->
                            val rank = index + 1
                            val isUser = rank == manager.currentRank
                            val isTarget = if (manager.currentRank == 1) rank == 2
                            else (manager.isDefending && rank == manager.currentRank + 1) || (!manager.isDefending && rank == manager.currentRank - 1)

                            val isRevealed = rank >= manager.highestRank || (rank == 14 && manager.highestRank == 15) || isTarget
                            val isHighest = rank == manager.highestRank
                            val isDefeated = rank > manager.currentRank && !isTarget

                            val effectiveOppRank = if (rank > manager.currentRank) rank - 1 else rank
                            val opponent = TournamentRoster.getOpponent(effectiveOppRank) ?: TournamentRoster.opponents.values.last()

                            val playerName = appSettings?.playerName ?: "CHALLENGER"
                            val playerTitle = appSettings?.playerTitle ?: "Novice"
                            val oppName = stringResource(id = opponent.nameRes)
                            val youSuffix = stringResource(id = R.string.player_you_suffix)

                            val isLocked = !gameViewModel.isPremiumVersion && rank <= 14

                            TournamentRowItem(
                                rank = rank,
                                name = if (isRevealed) (if (isUser) "$playerName $youSuffix" else oppName) else stringResource(id = R.string.tournament_unknown_name),
                                title = if (isRevealed) (if (isUser) playerTitle else stringResource(id = opponent.titleRes)) else stringResource(id = R.string.tournament_unknown_title),
                                isUser = isUser,
                                isTarget = isTarget,
                                isHighest = isHighest,
                                isDefeated = isDefeated,
                                isRevealed = isRevealed,
                                isLocked = isLocked,
                                accentColor = accentColor,
                                onInfoClick = {
                                    soundManager.playClick()
                                    if (isUser) showPlayerStats = true else opponentToShow = opponent
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 4. GOMBOK ---
                Row(
                    modifier = Modifier.fillMaxWidth(0.66f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RiposteSystemButton(
                        text = stringResource(id = R.string.btn_back),
                        modifier = Modifier.size(height = 56.dp, width = 80.dp),
                        onClick = { soundManager.playClick(); onBackToMenu() }
                    )

                    val pulse = rememberInfiniteTransition(label = "btnPulse").animateFloat(
                        initialValue = 1f, targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutSine), RepeatMode.Reverse), label = "pulse"
                    )

                    val buttonText = if (hasOngoingMatch) stringResource(id = R.string.btn_resume_match)
                    else if (manager.isDefending || manager.currentRank == 1) stringResource(id = R.string.btn_accept_challenge)
                    else stringResource(id = R.string.btn_en_garde)

                    RiposteSystemButton(
                        text = buttonText,
                        modifier = Modifier.weight(1f).height(56.dp).graphicsLayer { scaleX = pulse.value; scaleY = pulse.value },
                        isHanging = true,
                        onClick = {
                            soundManager.playClick()
                            if (hasOngoingMatch) {
                                appSettings?.let { settings -> gameViewModel.loadTournamentState(settings) }
                                onResumeGame()
                            } else if (!gameViewModel.isPremiumVersion && manager.currentRank == 15 && !manager.isDefending) {
                                showPremiumDialog = true
                            } else {
                                onStartMatch()
                            }
                        }
                    )
                }
            }

            OpponentCardOverlay(opponent = opponentToShow, isVisible = opponentToShow != null, onClose = { soundManager.playClick(); opponentToShow = null })

            if (showPlayerStats && appSettings != null) {
                PlayerStatsOverlay(
                    appSettings = appSettings!!,
                    isFirstLaunch = false,
                    soundManager = soundManager,
                    onSaveProfile = { newName, newTitle ->
                        coroutineScope.launch { settingsManager.updateSettings(appSettings!!.copy(playerName = newName, playerTitle = newTitle)) }
                    },
                    onDismiss = { showPlayerStats = false }
                )
            }
            if (showPremiumDialog) { PremiumUnlockDialog(soundManager = soundManager, onDismiss = { showPremiumDialog = false }) }
        }
    }
}

@Composable
fun TournamentRowItem(
    rank: Int, name: String, title: String,
    isUser: Boolean, isTarget: Boolean, isHighest: Boolean, isDefeated: Boolean, isRevealed: Boolean, isLocked: Boolean,
    accentColor: Color, onInfoClick: () -> Unit
) {
    val alpha = if (isLocked && !isTarget) 0.3f else if (!isRevealed) 0.15f else if (isDefeated) 0.4f else 1f

    val bgBrush = if (isUser) Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.3f), Color.Transparent))
    else if (isTarget) Brush.horizontalGradient(listOf(Color(0xFFFF5555).copy(alpha = 0.2f), Color.Transparent))
    else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(bgBrush)
                .border(
                    width = if (isUser) 1.5.dp else 1.dp,
                    color = if (isUser) accentColor else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                )
                .clickable(enabled = isRevealed) { onInfoClick() }
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .graphicsLayer { this.alpha = alpha },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy((-3).dp)
            ) {
                Text(
                    text = name,
                    color = if (isUser || isRevealed) accentColor.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    color = if (isUser || isRevealed) accentColor.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isLocked && !isUser) Text("🔒", fontSize = 14.sp)

                if (isTarget) {
                    val blink = rememberInfiniteTransition(label = "").animateFloat(
                        initialValue = 0.2f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = ""
                    )
                    Text(text = "⚔️", fontSize = 16.sp, modifier = Modifier.graphicsLayer { this.alpha = blink.value })
                }
            }
        }
    }
}