package hu.riposte.game.engine.logic

import hu.riposte.game.engine.data.MoveData

expect object NativeEngine {
    fun getBestStep(
        p1: Long,
        p2: Long,
        p: Int,
        d: Int,
        r: Boolean,
        sep: Int,
        offW: Int,
        defW: Int
    ): MoveData

    fun getBestStepTT(
        p1: Long,
        p2: Long,
        p: Int,
        d: Int,
        r: Boolean,
        sep: Int,
        offW: Int,
        defW: Int
    ): MoveData
}
