package hu.riposte.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// --- Adatmodellek ---
enum class Difficulty(val label: String, val depth: Int) {
    BEGINNER("Beg", 5),
    NORMAL("Std", 7),
    HARD("Hard", 9),
    MASTER("Mast", 10)
}

class MainActivity : ComponentActivity() {

    external fun getBestStep(board: IntArray, playerID: Int, depth: Int, riposte: Boolean): IntArray

    companion object {
        init {
            System.loadLibrary("riposte")
        }
    }

    private var boardState by mutableStateOf(intArrayOf(
        1, 1, 1, 1, 1,
        0, 0, 0, 0, 0,
        0, 0, 0, 0, 0,
        0, 0, 4, 0, 0,
        0, 0, 0, 0, 0,
        0, 0, 0, 0, 0,
        2, 2, 2, 2, 2
    ))

    private var isAiThinking by mutableStateOf(false)
    private var selectedDifficulty by mutableStateOf(Difficulty.NORMAL)
    private var isRiposteEnabled by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    val scrollState = rememberScrollState()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(scrollState) // Gombok elérése görgetéssel
                    ) {
                        Text(
                            text = "Riposte Engine v1.1",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // A Tábla - fix aránnyal, hogy ne nyomja össze a UI-t
                        Box(modifier = Modifier.fillMaxWidth().aspectRatio(5f/7f)) {
                            RiposteBoard(boardState)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Beállítások kártya
                        SettingsCard(
                            difficulty = selectedDifficulty,
                            onDifficultyChange = { selectedDifficulty = it },
                            riposte = isRiposteEnabled,
                            onRiposteChange = { isRiposteEnabled = it },
                            enabled = !isAiThinking
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // AI Indító Gomb
                        Button(
                            onClick = { executeAiMove() },
                            enabled = !isAiThinking,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3D5AFE),
                                disabledContainerColor = Color(0xFF283593)
                            )
                        ) {
                            if (isAiThinking) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text("AI LÉPÉS INDÍTÁSA", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp)) // Alsó margó
                    }
                }
            }
        }
    }

    private fun executeAiMove() {
        isAiThinking = true
        lifecycleScope.launch(Dispatchers.Default) {
            val result = getBestStep(boardState, 1, selectedDifficulty.depth, isRiposteEnabled)
            withContext(Dispatchers.Main) {
                updateBoard(result)
                isAiThinking = false
            }
        }
    }

    private fun updateBoard(move: IntArray) {
        if (move.size < 6) return
        val newBoard = boardState.copyOf()
        val fromIdx = move[1] * 5 + move[0]
        val toIdx   = move[3] * 5 + move[2]
        val hsIdx   = move[5] * 5 + move[4]

        if (fromIdx in 0..34 && toIdx in 0..34) {
            val piece = newBoard[fromIdx]
            newBoard[fromIdx] = 0
            for (i in newBoard.indices) { if (newBoard[i] == 4) newBoard[i] = 0 }
            newBoard[toIdx] = piece
            if (hsIdx in 0..34 && newBoard[hsIdx] == 0) newBoard[hsIdx] = 4
        }
        boardState = newBoard
    }
}

@Composable
fun RiposteBoard(board: IntArray) {
    Box(modifier = Modifier.fillMaxSize().border(2.dp, Color(0xFF444444))) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false // A belső görgetést tiltjuk, hogy a külső működjön
        ) {
            items(35) { index ->
                val cell = board[index]
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .border(0.5.dp, Color(0xFF222222))
                        .background(if (cell == 4) Color(0x22FFEB3B) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    when (cell) {
                        1 -> FencerPiece(Color(0xFF448AFF)) // AI Kék
                        2 -> FencerPiece(Color(0xFF00C853)) // Player Zöld
                        4 -> Box(Modifier.size(10.dp).background(Color(0xFFFFEB3B), CircleShape))
                    }
                }
            }
        }
    }
}

@Composable
fun FencerPiece(color: Color) {
    Surface(
        modifier = Modifier.size(34.dp),
        shape = CircleShape,
        color = color,
        tonalElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize().border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCard(
    difficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    riposte: Boolean,
    onRiposteChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("AI SZINT", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Difficulty.values().forEach { level ->
                    FilterChip(
                        selected = difficulty == level,
                        onClick = { if (enabled) onDifficultyChange(level) },
                        label = { Text(level.label, fontSize = 11.sp) },
                        enabled = enabled
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = riposte,
                    onCheckedChange = { if (enabled) onRiposteChange(it) },
                    enabled = enabled
                )
                Text(
                    text = if (riposte) "Riposte engedélyezve" else "Clean Cut szabály",
                    color = Color.White,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}