package hu.riposte.game.engine.logic

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import hu.riposte.game.engine.data.TournamentOpponent
import hu.riposte.game.engine.data.TournamentRoster

class TournamentManager {
    var currentRank by mutableStateOf(20)
        private set
    var highestRank by mutableStateOf(20)
        private set
    var isDefending by mutableStateOf(false)
        private set

    var matchHistoryList by mutableStateOf<List<Int>>(emptyList())
        private set
    var riposteRating by mutableStateOf(0)
        private set

    // ÚJ: Memóriában tartott Peak Rating
    var highestRating by mutableStateOf(1000)
        private set

    // JAVÍTÁS: Aláírás bővítve a highestRatingScore paraméterrel
    fun loadState(rank: Int, highest: Int, defending: Boolean, historyString: String = "", highestRatingScore: Int = 1000) {
        currentRank = rank.coerceIn(1, 20)
        highestRank = highest.coerceIn(1, 20)
        isDefending = defending
        highestRating = highestRatingScore // <-- Beolvassuk a mentett rekordot

        matchHistoryList = if (historyString.isEmpty()) {
            emptyList()
        } else {
            historyString.split(",").mapNotNull { it.toIntOrNull() }
        }
        riposteRating = matchHistoryList.sum()
    }

    fun getNextOpponent(): TournamentOpponent {
        if (currentRank == 20) isDefending = false

        val targetRank = if (isDefending) {
            currentRank
        } else {
            (currentRank - 1).coerceAtLeast(1)
        }

        return TournamentRoster.getOpponent(targetRank)
            ?: TournamentRoster.opponents.values.maxByOrNull { it.rank }!!
    }

    fun onMatchFinished(isVictory: Boolean, matchScore: Int): String {
        if (isDefending) {
            if (isVictory) isDefending = false else {
                if (currentRank < 20) currentRank++
                isDefending = true
            }
        } else {
            if (isVictory) {
                if (currentRank > 1) currentRank--
                if (currentRank < highestRank) highestRank = currentRank
                isDefending = false
            } else {
                isDefending = true
            }
        }

        val updatedList = matchHistoryList.toMutableList()
        updatedList.add(matchScore)
        matchHistoryList = updatedList.takeLast(10)

        riposteRating = matchHistoryList.sum()

        // ÚJ: Megdőlt a pontrekord? Ha igen, írjuk felül a memóriában!
        if (riposteRating > highestRating) {
            highestRating = riposteRating
        }

        return matchHistoryList.joinToString(",")
    }

    fun resetTournament() {
        currentRank = 20
        highestRank = 20
        isDefending = false
        matchHistoryList = emptyList()
        riposteRating = 0
        highestRating = 1000 // Alapértelmezett pontszám
    }
}