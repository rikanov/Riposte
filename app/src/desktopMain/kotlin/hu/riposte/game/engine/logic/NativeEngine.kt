package hu.riposte.game.engine.logic

import hu.riposte.game.engine.data.MoveData

actual object NativeEngine {
    init {
        try {
            System.loadLibrary("riposte")
        } catch (e: UnsatisfiedLinkError) {
            // Fallback for local development via Gradle :app:run
            val projectDir = java.io.File(System.getProperty("user.dir"))
            // The run task might execute from the root or the /app dir, so we check the path
            val libPath = if (projectDir.name == "app") {
                java.io.File(projectDir, "src/cpp/build/libriposte.so")
            } else {
                java.io.File(projectDir, "app/src/cpp/build/libriposte.so")
            }
            if (libPath.exists()) {
                System.load(libPath.absolutePath)
            } else {
                println("Warning: Native library not found at ${libPath.absolutePath}. AI will not work.")
            }
        }
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
