package hu.riposte.game.engine.logic

import hu.riposte.game.engine.data.MoveData

actual object NativeEngine {
    init {
        System.loadLibrary("riposte")
    }

    actual fun getBestStep(
        p1: Long,
        p2: Long,
        p: Int,
        d: Int,
        r: Boolean,
        sep: Int,
        offW: Int,
        defW: Int
    ): MoveData = getBestStepNative(p1, p2, p, d, r, sep, offW, defW)

    actual fun getBestStepTT(
        p1: Long,
        p2: Long,
        p: Int,
        d: Int,
        r: Boolean,
        sep: Int,
        offW: Int,
        defW: Int
    ): MoveData = getBestStepNativeTT(p1, p2, p, d, r, sep, offW, defW)

    private external fun getBestStepNative(p1: Long, p2: Long, p: Int, d: Int, r: Boolean, sep: Int, offW: Int, defW: Int): MoveData
    private external fun getBestStepNativeTT(p1: Long, p2: Long, p: Int, d: Int, r: Boolean, sep: Int, offW: Int, defW: Int): MoveData
}
