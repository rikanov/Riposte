package hu.riposte.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RiposteGameApp(gameViewModel: GameViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Riposte",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            when(gameViewModel.gamePhase)
            {
                GameViewModel.GameWaitingFor.MOVE_PIECE -> Text("Player's turn", color = Color.Blue, fontWeight = FontWeight.Bold)
                GameViewModel.GameWaitingFor.TAKE_PIECE -> Text("Select an opponent's piece to capture", color = Color.Blue)
                GameViewModel.GameWaitingFor.GAME_OVER -> Text("Game Over", color = Color.White, fontWeight = FontWeight.Bold)
                GameViewModel.GameWaitingFor.AI_MOVE -> Text("AI's turn", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(2.dp, Color.DarkGray)
                .background(Color.Gray)
        ) {
            for (row in 0 until 7) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0 until 5) {
                        val index = row * 5 + col
                        val cellValue = gameViewModel.board[index]

                        BoardCell(value = cellValue,index, gameViewModel, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        if (gameViewModel.gamePhase == GameViewModel.GameWaitingFor.GAME_OVER) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Game finished") },
                text = { Text(gameViewModel.winner ?: "") },
                confirmButton = {
                    Button(onClick = { gameViewModel.resetGame() }) {
                        Text("New Game")
                    }
                }
            )
        }
    }
}

@Composable
fun BoardCell(value: Int, index: Int, viewModel: GameViewModel, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(0.5.dp, Color.LightGray)
            .background(Color(0xFFE0E0E0))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { /*  */ },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        viewModel.handleSwipe(index, dragAmount)
                    }
                )
            }
            .clickable {
                viewModel.onCellClick(index)
            },
        contentAlignment = Alignment.Center
    ) {
        when (value) {
            1 -> Piece(color = Color.Red)
            2 -> Piece(color = Color.Blue)
            4 -> HotSpotGraphic()
        }
    }
}

@Composable
fun Piece(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize(0.8f)
            .background(color, shape = CircleShape)
            .border(2.dp, Color.White, shape = CircleShape)
    )
}

@Composable
fun HotSpotGraphic() {
    Box(
        modifier = Modifier
            .fillMaxSize(0.7f)
            .background(Color.Yellow, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .border(3.dp, Color(0xFFB8860B), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("★", color = Color(0xFFB8860B), fontWeight = FontWeight.Bold)
    }
}