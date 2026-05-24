package hu.riposte.game.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import riposte.app.generated.resources.*
import org.jetbrains.compose.resources.*
import hu.riposte.game.ui.components.RiposteSystemButton
import hu.riposte.game.ui.components.VolumetricLightOrgan
import hu.riposte.game.ui.theme.LocalGameTheme

@Composable
fun GameOverOverlay(
    isWin: Boolean,
    isTimeOut: Boolean,
    isTournamentMode: Boolean,
    isReviewingGame: Boolean,
    opponentRank: Int? = null,
    onStartReview: () -> Unit,
    onStopReview: () -> Unit,
    onRematch: () -> Unit,
    onMainMenu: () -> Unit,
    onContinueTournament: () -> Unit
) {
    val theme = LocalGameTheme.current
    val accentColor = theme.uiAccentColor

    val quotes = if (isWin) theme.victoryQuotes else theme.defeatQuotes
    val quoteResId = remember(isTournamentMode, opponentRank, isWin) {
        if (isTournamentMode && opponentRank != null) {
            val opponent = hu.riposte.game.engine.data.TournamentRoster.getOpponent(opponentRank)
            if (opponent != null) {
                if (isWin) opponent.quoteWin else opponent.quoteLose
            } else {
                if (isWin) Res.string.quote_tourney_generic_win else Res.string.quote_tourney_generic_lose
            }
        } else {
            val quotes = if (isWin) theme.victoryQuotes else theme.defeatQuotes
            if (quotes.isNotEmpty()) quotes.random() else Res.string.game_over_victory
        }
    }
    val mainText = if (isTimeOut) stringResource(Res.string.game_over_timeout)
    else if (isWin) stringResource(Res.string.game_over_victory)
    else stringResource(Res.string.game_over_defeat)

    val mainTextColor = if (isWin) accentColor else Color(0xFFFF4444)

    val infiniteTransition = rememberInfiniteTransition(label = "GameOverPulse")
    val textScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "textScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !isReviewingGame,
            enter = fadeIn(tween(800)),
            exit = fadeOut(tween(400))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.maxDimension * 0.7f

                val centerColor = if (isWin) accentColor.copy(alpha = 0.3f) else theme.auraP1Color.copy(alpha = 0.4f)
                val edgeColor = Color.Black.copy(alpha = 0.9f)

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(centerColor, edgeColor),
                        center = center,
                        radius = radius
                    )
                )
            }
            if (isWin) {
                VolumetricLightOrgan(
                    accentColor = accentColor,
                    centerYRatio = 0.5f
                )
                FireworksOverlay()
            }
        }
        AnimatedVisibility(
            visible = !isReviewingGame,
            enter = fadeIn(tween(800)),
            exit = fadeOut(tween(400)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF151921).copy(alpha = 0.95f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mainText,
                    color = mainTextColor,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    fontFamily = theme.fontFamily,
                    modifier = Modifier.graphicsLayer { scaleX = textScale; scaleY = textScale }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(quoteResId),
                    color = theme.textColor,
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = theme.fontFamily,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                RiposteSystemButton(
                    text = stringResource(Res.string.btn_review_board),
                    onClick = onStartReview,
                    isHanging = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (isTournamentMode) {
                    RiposteSystemButton(
                        text = stringResource(Res.string.btn_continue_tournament),
                        onClick = onContinueTournament,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RiposteSystemButton(
                            text = stringResource(Res.string.btn_main_menu),
                            onClick = onMainMenu,
                            modifier = Modifier.weight(1f).height(50.dp)
                        )
                        RiposteSystemButton(
                            text = stringResource(Res.string.btn_rematch),
                            onClick = onRematch,
                            modifier = Modifier.weight(1f).height(50.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isReviewingGame,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { onStopReview() }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.btn_return_to_summary),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = theme.fontFamily
                )
            }
        }
    }
}