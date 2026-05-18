package hu.riposte.game.engine.logic

import hu.riposte.game.engine.data.MoveData

actual object NativeEngine {
    actual fun getBestStep(
        p1: Long,
        p2: Long,
        p: Int,
        d: Int,
        r: Boolean,
        sep: Int,
        offW: Int,
        defW: Int
    ): MoveData = MoveData(-1, -1, -1)

    actual fun getBestStepTT(
        p1: Long,
        p2: Long,
        p: Int,
        d: Int,
        r: Boolean,
        sep: Int,
        offW: Int,
        defW: Int
    ): MoveData = MoveData(-1, -1, -1)
}
