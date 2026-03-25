package hu.riposte.game

import android.app.Activity
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RiposteGameApp(gameViewModel: GameViewModel = viewModel()) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = (context as? Activity)
    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

                Text("Riposte", style = MaterialTheme.typography.headlineMedium)

                Box{

                    IconButton(onClick = { showMenu = true }) {

                        Icon(Icons.Default.MoreVert, "Menu")

                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("New Game") },
                            onClick = { gameViewModel.gamePhase = GameWaitingFor.SETUP; showMenu = false }
                        )

                        DropdownMenuItem(
                            text = { Text("Undo") },
                            onClick = { gameViewModel.undo(); showMenu = false },
                            enabled = gameViewModel.gamePhase == GameWaitingFor.MOVE_PIECE
                        )

                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        DropdownMenuItem(
                            text = { Text("Exit Game" ) },
                            onClick = { activity?.finish(); showMenu = false }
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                when(gameViewModel.gamePhase)
                {

                    GameWaitingFor.MOVE_PIECE -> Text("Your Turn", color = Color.Blue, fontWeight = FontWeight.Bold)
                    GameWaitingFor.AI_MOVE -> Text("Ai is thinking ...", color = Color.Red, fontWeight = FontWeight.Bold)
                    GameWaitingFor.TAKE_PIECE -> Text("Touche! Select an opponent's piece to capture", color = Color.Blue)
                    GameWaitingFor.GAME_OVER -> Text("Game Over", color = Color.Red, fontWeight = FontWeight.Bold)
                    else -> Text(" < < > >", color = Color.Red)
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().aspectRatio(5f/7f).border(2.dp, Color.DarkGray)) {
                Column(modifier = Modifier.fillMaxSize().background(Color.Gray)) {
                    for (row in 0 until 7) {
                        Row(modifier = Modifier.weight(1f)) {
                            for (col in 0 until 5) {
                                BoardCell(gameViewModel.board[row*5+col], row*5+col, gameViewModel, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        if (gameViewModel.gamePhase == GameWaitingFor.SETUP) {
            SetupDialog(
                onStart = { gameViewModel.startNewGame(it) },
                onCancel = { gameViewModel.gamePhase = GameWaitingFor.MOVE_PIECE }
            )
        }

        if (gameViewModel.gamePhase == GameWaitingFor.GAME_OVER) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Result") },
                text = { Text(gameViewModel.winner ?: "") },
                confirmButton = {
                    Button(onClick = {
                        gameViewModel.restartGame()
                    }) { Text("OK") } }
            )
        }
    }
}

@Composable
fun SetupDialog(
    onStart: (GameSettings) -> Unit,
    onCancel: () -> Unit
) {
    var diff by remember { mutableFloatStateOf(5f) }
    var riposte by remember { mutableStateOf(true) }
    var starter by remember { mutableStateOf(StartingPlayer.PLAYER) }
    var selectedMode by remember { mutableStateOf(GameMode.VS_AI) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Game Settings") },
        text = {
            Column {
                Text("Game Mode")
                Row {
                    GameMode.entries.forEach { mode ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedMode = mode }) {
                            RadioButton(selected = (selectedMode == mode), onClick = { selectedMode = mode })
                            Text(if(mode == GameMode.VS_AI) "vs Computer" else "2 Players", fontSize = 12.sp)
                        }
                    }
                }
                if( selectedMode == GameMode.VS_PLAYER ) {
                    starter = StartingPlayer.PLAYER
                }
                Text("Difficulty (Depth): ${diff.toInt()}",
                    color = if (selectedMode == GameMode.VS_AI) Color.Unspecified else Color.Gray)
                Slider(value = diff,
                    onValueChange = { diff = it },
                    valueRange = 5f..10f,
                    steps = 4,
                    enabled = selectedMode == GameMode.VS_AI
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = riposte, onCheckedChange = { riposte = it })
                    Text("Enable Riposte Rule")
                }
                Spacer(modifier = Modifier.height(8.dp))
                if( selectedMode == GameMode.VS_AI ) {
                    Text(
                        "Who starts?",
                        color = if (selectedMode == GameMode.VS_AI) Color.Unspecified else Color.Gray
                    )
                    Row {
                        StartingPlayer.entries.forEach { op ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { starter = op }.padding(end = 8.dp)
                            ) {
                                RadioButton(
                                    selected = (starter == op),
                                    onClick = { starter = op })
                                Text(op.name.lowercase(), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onStart(GameSettings(selectedMode, starter, diff.toInt(), riposte)) }) {
                Text("Start Fight!")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BoardCell(value: Int, index: Int, viewModel: GameViewModel, modifier: Modifier) {
    val isDark = ((index/5) + (index%5)) % 2 == 0
    Box(modifier = modifier.aspectRatio(1f).background(if(isDark) Color(0xFFDBDBDB) else Color(0xFFE5E5E5))
        .pointerInput(Unit) { detectDragGestures(onDrag = { change, drag -> change.consume();
            viewModel.handleSwipe(index, drag) }) }
        .clickable { viewModel.onCellClick(index) }, contentAlignment = Alignment.Center
    ) {
        when(value) {
            1 -> Box(Modifier.fillMaxSize(0.8f).background(Color(0xFFD32F2F), CircleShape).border(2.dp, Color.White, CircleShape))
            2 -> Box(Modifier.fillMaxSize(0.8f).background(Color(0xFF1976D2), CircleShape).border(2.dp, Color.White, CircleShape))
            4 -> Box(Modifier.fillMaxSize(0.6f).background(Color.Yellow, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) { Text("★") }
        }
    }
}

