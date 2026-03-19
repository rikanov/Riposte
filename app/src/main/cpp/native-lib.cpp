#include <jni.h>
#include "riposte_engine.h"

extern "C" JNIEXPORT jintArray JNICALL
Java_hu_riposte_game_MainActivity_getBestStep(
        JNIEnv* env,
        jobject thiz,
        jintArray board,
        jint playerID,
        jint depth,      // ÚJ paraméter fogadása
        jboolean riposte // ÚJ paraméter fogadása
) {
    // 1. A tábla lekérése a memóriából
    jint* boardPtr = env->GetIntArrayElements(board, nullptr);

    // 2. Meghívjuk a te Engine-edet az új értékekkel
    // A jboolean-t (bool)-ra kényszerítjük, a jint-et (uint)-ra
    MoveData move = RiposteEngine::getBestStep(
            boardPtr,
            (int)playerID,
            (uint)depth,
            (bool)riposte
    );

    // 3. Az eredmény (6 koordináta) visszaküldése
    jintArray result = env->NewIntArray(6);
    env->SetIntArrayRegion(result, 0, 6, (jint*)move.m_array);

    // 4. Memória felszabadítása
    env->ReleaseIntArrayElements(board, boardPtr, JNI_ABORT);

    return result;
}