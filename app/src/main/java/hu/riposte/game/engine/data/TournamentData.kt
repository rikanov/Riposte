package hu.riposte.game.engine.data

import androidx.annotation.StringRes
import hu.riposte.game.R

data class TournamentOpponent(
    val rank: Int,
    @StringRes val nameRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val eraRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val quoteRes: Int,
    val engineDepth: Int,
    val offensiveWeight: Int = 10,
    val defensiveWeight: Int = 10
)

object TournamentRoster {

    val opponents: Map<Int, TournamentOpponent> = listOf(

        // --- Stygian (Rank 1) ---
        TournamentOpponent(1, R.string.opp_1_name, R.string.opp_1_title, R.string.opp_1_era, R.string.opp_1_desc, R.string.opp_1_quote, 9, 12, 14),

        // --- MASTERS (Rank 2 - 9) ---
        TournamentOpponent(2, R.string.opp_2_name, R.string.opp_2_title, R.string.opp_2_era, R.string.opp_2_desc, R.string.opp_2_quote, 8, 10, 15),
        TournamentOpponent(3, R.string.opp_3_name, R.string.opp_3_title, R.string.opp_3_era, R.string.opp_3_desc, R.string.opp_3_quote, 8, 14, 10),
        TournamentOpponent(4, R.string.opp_4_name, R.string.opp_4_title, R.string.opp_4_era, R.string.opp_4_desc, R.string.opp_4_quote, 7, 8, 16),
        TournamentOpponent(5, R.string.opp_5_name, R.string.opp_5_title, R.string.opp_5_era, R.string.opp_5_desc, R.string.opp_5_quote, 7, 18, 5),
        TournamentOpponent(6, R.string.opp_6_name, R.string.opp_6_title, R.string.opp_6_era, R.string.opp_6_desc, R.string.opp_6_quote, 7, 10, 13),
        TournamentOpponent(7, R.string.opp_7_name, R.string.opp_7_title, R.string.opp_7_era, R.string.opp_7_desc, R.string.opp_7_quote, 7, 16, 7),
        TournamentOpponent(8, R.string.opp_8_name, R.string.opp_8_title, R.string.opp_8_era, R.string.opp_8_desc, R.string.opp_8_quote, 6, 7, 14),
        TournamentOpponent(9, R.string.opp_9_name, R.string.opp_9_title, R.string.opp_9_era, R.string.opp_9_desc, R.string.opp_9_quote, 6, 15, 8),

        // --- DUELISTS (Rank 10 - 15) ---
        TournamentOpponent(10, R.string.opp_10_name, R.string.opp_10_title, R.string.opp_10_era, R.string.opp_10_desc, R.string.opp_10_quote, 5, 8, 12),
        TournamentOpponent(11, R.string.opp_11_name, R.string.opp_11_title, R.string.opp_11_era, R.string.opp_11_desc, R.string.opp_11_quote, 5, 14, 8),
        TournamentOpponent(12, R.string.opp_12_name, R.string.opp_12_title, R.string.opp_12_era, R.string.opp_12_desc, R.string.opp_12_quote, 5, 10, 11),
        TournamentOpponent(13, R.string.opp_13_name, R.string.opp_13_title, R.string.opp_13_era, R.string.opp_13_desc, R.string.opp_13_quote, 4, 13, 7),
        TournamentOpponent(14, R.string.opp_14_name, R.string.opp_14_title, R.string.opp_14_era, R.string.opp_14_desc, R.string.opp_14_quote, 3, 11, 9),
        TournamentOpponent(15, R.string.opp_15_name, R.string.opp_15_title, R.string.opp_15_era, R.string.opp_15_desc, R.string.opp_15_quote, 7, 0, 0),

        // --- NOVICES (Rank 16 - 19) ---
        TournamentOpponent(16, R.string.opp_16_name, R.string.opp_16_title, R.string.opp_16_era, R.string.opp_16_desc, R.string.opp_16_quote, 5, 0, 0),
        TournamentOpponent(17, R.string.opp_17_name, R.string.opp_17_title, R.string.opp_17_era, R.string.opp_17_desc, R.string.opp_17_quote, 3, 0, 0),
        TournamentOpponent(18, R.string.opp_18_name, R.string.opp_18_title, R.string.opp_18_era, R.string.opp_18_desc, R.string.opp_18_quote, 3, 0, 0),
        TournamentOpponent(19, R.string.opp_19_name, R.string.opp_19_title, R.string.opp_19_era, R.string.opp_19_desc, R.string.opp_19_quote, 3, 0, 0)
    ).associateBy { it.rank }

    fun getOpponent(rank: Int): TournamentOpponent? {
        return opponents[rank]
    }
}
