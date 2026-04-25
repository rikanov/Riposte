#include <jni.h>
#include "riposte_engine.h"
#include "riposte_tt_engine.h"
#include <android/log.h>
#define LOG_TAG "RiposteEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {
JNIEXPORT jobject JNICALL
Java_hu_riposte_game_engine_logic_GameViewModel_getBestStepNative(
        JNIEnv *env,
        jobject thiz,
        jintArray jBoard,
        jint playerId,
        jint depth,
        jboolean isRiposteAllowed) {
    LOGI("Start function...");
    jint *boardPtr = env->GetIntArrayElements(jBoard, nullptr);
    int board[35];
    for (int i = 0; i < 35; i++) board[i] = boardPtr[i];

    LOGI("Start JNI call..");
    MoveData result = RiposteEngine::getBestStep(board, (int) playerId, (int) depth,
                                                 (bool) isRiposteAllowed);
    LOGI("Engine finished: from=%d, to=%d, hs=%d", result[0], result[1], result[2]);

    env->ReleaseIntArrayElements(jBoard, boardPtr, JNI_ABORT);

    jclass moveDataClass = env->FindClass("hu/riposte/game/engine/data/MoveData");
    jmethodID constructor = env->GetMethodID(moveDataClass, "<init>", "(III)V");

    return env->NewObject(moveDataClass, constructor,
                          (jint)result[0],  // from
                          (jint)result[1],  // to
                          (jint)result[2]); // hotspot
}
}

extern "C" JNIEXPORT jobject JNICALL
Java_hu_riposte_game_engine_logic_GameViewModel_getBestStepNativeTT(
        JNIEnv *env,
        jobject /* this */,
        jintArray b,
        jint p,
        jint d,
        jboolean r) {

    jint *board = env->GetIntArrayElements(b, nullptr);
    MoveData bestMove = Riposte_TT_Engine::getBestStep(board, p, d, r);

    env->ReleaseIntArrayElements(b, board, JNI_ABORT);

    jclass moveDataClass = env->FindClass("hu/riposte/game/engine/data/MoveData");
    jmethodID constructor = env->GetMethodID(moveDataClass, "<init>", "(III)V");

    return env->NewObject(moveDataClass, constructor, bestMove[0], bestMove[1], bestMove[2]);
}