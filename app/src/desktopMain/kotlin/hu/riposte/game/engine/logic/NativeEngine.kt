package hu.riposte.game.engine.logic

import hu.riposte.game.engine.data.MoveData
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

actual object NativeEngine {
    init {
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val libName = if (isWindows) "riposte.dll" else "libriposte.so"

        try {
            // 1. Developer (IDE) path - Fallback for local development via Gradle :app:run
            val projectDir = File(System.getProperty("user.dir"))
            val devPath = if (projectDir.name == "app") {
                File(projectDir, "src/cpp/build/$libName")
            } else {
                File(projectDir, "app/src/cpp/build/$libName")
            }

            if (devPath.exists()) {
                System.load(devPath.absolutePath)
            } else {
                val inputStream = NativeEngine::class.java.getResourceAsStream("/$libName")
                    ?: throw RuntimeException(" $libName not found!")

                val tempFile = File.createTempFile("riposte_ai_", if (isWindows) ".dll" else ".so")
                tempFile.deleteOnExit()
                Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                System.load(tempFile.absolutePath)
            }
        } catch (e: Exception) {
            System.err.println("CRITICAL JNI ERROR: ${e.message}")
            e.printStackTrace()
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