package hu.riposte.game.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.riposte.game.R
import hu.riposte.game.engine.logic.SoundManager
import hu.riposte.game.ui.theme.LocalGameTheme
import kotlinx.coroutines.delay

@Composable
fun AnimatedOdometer(
    value: Int,
    digitCount: Int,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "OdometerTween"
    )

    val valueString = animatedValue.toString().padStart(digitCount, '0')

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        modifier = Modifier.padding(horizontal = 0.dp)
    ) {
        valueString.forEach { digit ->
            DigitDrum(digit = digit, color = color, fontSize = fontSize)
        }
    }
}

@Composable
fun DigitDrum(digit: Char, color: Color, fontSize: androidx.compose.ui.unit.TextUnit) {
    Box(
        modifier = Modifier
            .width(18.dp)
            .height(42.dp)
            .background(Color(0xFFE0E5EC)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = digit,
            transitionSpec = {
                (slideInVertically { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height } + fadeOut())
            },
            label = "DigitAnim"
        ) { targetDigit ->
            Text(
                text = targetDigit.toString(),
                color = color,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.5f),
                        0.5f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.5f)
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(0.5.dp)
                .background(Color.Black.copy(alpha = 0.1f))
        )
    }
}

@Composable
fun CylindricalBar(score: Float, maxScore: Float, accentColor: Color) {
    val animatedHeight by animateFloatAsState(
        targetValue = if (score == 0f) 0.05f else (score / maxScore).coerceIn(0.1f, 1.0f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "BarHeightAnimation"
    )

    val barBaseColor = if (score == 0f) Color.Transparent else if (score < 100f) Color(0xFFFF5555) else accentColor

    val cylinderBrush = Brush.horizontalGradient(
        0.0f to barBaseColor.copy(alpha = 0.3f),
        0.5f to barBaseColor,
        1.0f to barBaseColor.copy(alpha = 0.3f)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedHeight)
                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    .background(cylinderBrush)
                    .border(0.5.dp, Color.White.copy(alpha = if (score > 0f) 0.1f else 0f), RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(3.dp)
                .background(Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)))
        )
    }
}
@Composable
fun PlayerStatsOverlay(
    playerName: String,
    playerTitle: String,
    peakRating: Int,
    lastViewedRank: Int,
    currentRank: Int,
    lastViewedRating: Int,
    rating: Int,
    matchHistory: List<Int>,
    isFirstLaunch: Boolean,
    soundManager: SoundManager,
    onSaveProfile: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = LocalGameTheme.current.uiAccentColor

    var editName by remember(playerName) { mutableStateOf(playerName) }
    var editTitle by remember(playerTitle) { mutableStateOf(playerTitle) }
    var isEditing by remember { mutableStateOf(isFirstLaunch) }

    var displayRating by remember { mutableIntStateOf(lastViewedRating) }
    var displayRank by remember { mutableIntStateOf(lastViewedRank) }
    var chartReady by remember { mutableStateOf(false) }

    var activeTooltip by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        delay(300)
        displayRating = rating
        displayRank = currentRank
        chartReady = true
    }

    GlassDialog(onDismissRequest = { if (!isFirstLaunch) onDismiss() }) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isEditing) {
                BasicTextField(
                    value = editName,
                    onValueChange = { if (it.length <= 16) editName = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center),
                    cursorBrush = SolidColor(accentColor),
                    modifier = Modifier.fillMaxWidth().border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(8.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                BasicTextField(
                    value = editTitle,
                    onValueChange = { if (it.length <= 20) editTitle = it },
                    textStyle = TextStyle(color = accentColor, fontSize = 12.sp, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center),
                    cursorBrush = SolidColor(accentColor),
                    modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(6.dp)
                )
            } else {
                Text(
                    text = editName.uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.clickable { soundManager.playClick(); isEditing = true }
                )
                Text(
                    text = editTitle,
                    color = accentColor,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.clickable { soundManager.playClick(); isEditing = true }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.height(30.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = activeTooltip != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = activeTooltip ?: "",
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            val peakStr = stringResource(id = R.string.stat_peak_rating)
            val currentStr = stringResource(id = R.string.stat_current_rank)
            val ratingStr = stringResource(id = R.string.stat_rating)

            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // JAVÍTÁS: A sorrend most már 2 (Rank) -> 4 (Peak) -> 4 (Rating)
                listOf(
                    currentStr to (displayRank to 2),
                    peakStr to (peakRating to 4),
                    ratingStr to (displayRating to 4)
                ).forEach { (label, data) ->
                    val (valNum, digits) = data
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        activeTooltip = label
                                        soundManager.playClick()
                                        try { awaitRelease() } finally { activeTooltip = null }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedOdometer(
                            value = valNum,
                            digitCount = digits,
                            color = if (label == ratingStr) accentColor else Color(0xFF1E272E),
                            fontSize = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val formStr = stringResource(id = R.string.stat_recent_form)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color(0xFF05070A), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                activeTooltip = formStr
                                soundManager.playClick()
                                try { awaitRelease() } finally { activeTooltip = null }
                            }
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (matchHistory.isEmpty()) {
                    Text("NO MATCHES YET", color = Color.White.copy(alpha = 0.2f), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                } else {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxScore = maxOf(matchHistory.maxOrNull() ?: 100, 100).toFloat()
                        val displayList = List(10) { index ->
                            val offset = 10 - matchHistory.size
                            if (index >= offset) matchHistory[index - offset] else 0
                        }

                        displayList.forEach { score ->
                            Box(modifier = Modifier.weight(1f)) {
                                CylindricalBar(score = if (chartReady) score.toFloat() else 0f, maxScore = maxScore, accentColor = accentColor)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(accentColor).clickable {
                        soundManager.playClick()
                        isEditing = false
                        onSaveProfile(editName.ifBlank { "CHALLENGER" }, editTitle.ifBlank { "Unranked" })
                    }.padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(id = R.string.btn_save_profile), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                        .clickable { soundManager.playClick(); onDismiss() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(id = R.string.btn_close), color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}