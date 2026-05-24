package hu.riposte.game.engine.data

import org.jetbrains.compose.resources.StringResource
import riposte.app.generated.resources.*

data class TournamentOpponent(
    val rank: Int,
    val nameRes: StringResource,
    val titleRes: StringResource,
    val eraRes: StringResource,
    val descriptionRes: StringResource,
    val quoteRes: StringResource,
    val quoteWin: StringResource,
    val quoteLose: StringResource,
    val engineDepth: Int,
    val offensiveWeight: Int = 10,
    val defensiveWeight: Int = 10
)

object TournamentRoster {

    val opponents: Map<Int, TournamentOpponent> = listOf(

        // --- Stygian (Rank 1) ---
        TournamentOpponent(1, Res.string.opp_1_name, Res.string.opp_1_title, Res.string.opp_1_era, Res.string.opp_1_desc, Res.string.opp_1_quote, Res.string.opp_1_win, Res.string.opp_1_lose, 9, 12, 14),

        // --- MASTERS (Rank 2 - 9) ---
        TournamentOpponent(2, Res.string.opp_2_name, Res.string.opp_2_title, Res.string.opp_2_era, Res.string.opp_2_desc, Res.string.opp_2_quote, Res.string.opp_2_win, Res.string.opp_2_lose, 8, 10, 15),
        TournamentOpponent(3, Res.string.opp_3_name, Res.string.opp_3_title, Res.string.opp_3_era, Res.string.opp_3_desc, Res.string.opp_3_quote, Res.string.opp_3_win, Res.string.opp_3_lose, 8, 14, 10),
        TournamentOpponent(4, Res.string.opp_4_name, Res.string.opp_4_title, Res.string.opp_4_era, Res.string.opp_4_desc, Res.string.opp_4_quote, Res.string.opp_4_win, Res.string.opp_4_lose, 7, 8, 16),
        TournamentOpponent(5, Res.string.opp_5_name, Res.string.opp_5_title, Res.string.opp_5_era, Res.string.opp_5_desc, Res.string.opp_5_quote, Res.string.opp_5_win, Res.string.opp_5_lose, 7, 18, 5),
        TournamentOpponent(6, Res.string.opp_6_name, Res.string.opp_6_title, Res.string.opp_6_era, Res.string.opp_6_desc, Res.string.opp_6_quote, Res.string.opp_6_win, Res.string.opp_6_lose, 7, 10, 13),
        TournamentOpponent(7, Res.string.opp_7_name, Res.string.opp_7_title, Res.string.opp_7_era, Res.string.opp_7_desc, Res.string.opp_7_quote, Res.string.opp_7_win, Res.string.opp_7_lose, 7, 16, 7),
        TournamentOpponent(8, Res.string.opp_8_name, Res.string.opp_8_title, Res.string.opp_8_era, Res.string.opp_8_desc, Res.string.opp_8_quote, Res.string.opp_8_win, Res.string.opp_8_lose, 6, 7, 14),
        TournamentOpponent(9, Res.string.opp_9_name, Res.string.opp_9_title, Res.string.opp_9_era, Res.string.opp_9_desc, Res.string.opp_9_quote, Res.string.opp_9_win, Res.string.opp_9_lose, 6, 15, 8),

        // --- DUELISTS (Rank 10 - 15) ---
        TournamentOpponent(10, Res.string.opp_10_name, Res.string.opp_10_title, Res.string.opp_10_era, Res.string.opp_10_desc, Res.string.opp_10_quote, Res.string.opp_10_win, Res.string.opp_10_lose, 5, 8, 12),
        TournamentOpponent(11, Res.string.opp_11_name, Res.string.opp_11_title, Res.string.opp_11_era, Res.string.opp_11_desc, Res.string.opp_11_quote, Res.string.opp_11_win, Res.string.opp_11_lose, 5, 14, 8),
        TournamentOpponent(12, Res.string.opp_12_name, Res.string.opp_12_title, Res.string.opp_12_era, Res.string.opp_12_desc, Res.string.opp_12_quote, Res.string.opp_12_win, Res.string.opp_12_lose, 5, 10, 11),
        TournamentOpponent(13, Res.string.opp_13_name, Res.string.opp_13_title, Res.string.opp_13_era, Res.string.opp_13_desc, Res.string.opp_13_quote, Res.string.opp_13_win, Res.string.opp_13_lose, 4, 13, 7),
        TournamentOpponent(14, Res.string.opp_14_name, Res.string.opp_14_title, Res.string.opp_14_era, Res.string.opp_14_desc, Res.string.opp_14_quote, Res.string.opp_14_win, Res.string.opp_14_lose, 3, 11, 9),
        TournamentOpponent(15, Res.string.opp_15_name, Res.string.opp_15_title, Res.string.opp_15_era, Res.string.opp_15_desc, Res.string.opp_15_quote, Res.string.opp_15_win, Res.string.opp_15_lose, 7, 0, 0),

        // --- NOVICES (Rank 16 - 19) ---
        TournamentOpponent(16, Res.string.opp_16_name, Res.string.opp_16_title, Res.string.opp_16_era, Res.string.opp_16_desc, Res.string.opp_16_quote, Res.string.opp_16_win, Res.string.opp_16_lose, 5, 0, 0),
        TournamentOpponent(17, Res.string.opp_17_name, Res.string.opp_17_title, Res.string.opp_17_era, Res.string.opp_17_desc, Res.string.opp_17_quote, Res.string.opp_17_win, Res.string.opp_17_lose, 3, 0, 0),
        TournamentOpponent(18, Res.string.opp_18_name, Res.string.opp_18_title, Res.string.opp_18_era, Res.string.opp_18_desc, Res.string.opp_18_quote, Res.string.opp_18_win, Res.string.opp_18_lose, 3, 0, 0),
        TournamentOpponent(19, Res.string.opp_19_name, Res.string.opp_19_title, Res.string.opp_19_era, Res.string.opp_19_desc, Res.string.opp_19_quote, Res.string.opp_19_win, Res.string.opp_19_lose, 3, 0, 0)
    ).associateBy { it.rank }

    fun getOpponent(rank: Int): TournamentOpponent? {
        return opponents[rank]
    }
}