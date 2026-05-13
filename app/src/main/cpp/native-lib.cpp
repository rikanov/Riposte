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
        jlong p1,
        jlong p2,
        jint playerId,
        jint depth,
        jboolean isRiposteAllowed,
        jint sepLeft,
        jint offW,
        jint defW) {
    LOGI("Start JNI call..");
    MoveData result = RiposteEngine::getBestStep((uint64_t)p1, (uint64_t)p2, (int) playerId, (int) depth,
                                                 (bool) isRiposteAllowed, (int) sepLeft,
                                                 (int) offW, (int) defW);
    LOGI("Engine finished: from=%d, to=%d, hs=%d", result[0], result[1], result[2]);

    jclass moveDataClass = env->FindClass("hu/riposte/game/engine/data/MoveData");
    jmethodID constructor = env->GetMethodID(moveDataClass, "<init>", "(III)V");

    return env->NewObject(moveDataClass, constructor,
                          (jint)result[0],
                          (jint)result[1],
                          (jint)result[2]);
}
}

extern "C" JNIEXPORT jobject JNICALL
Java_hu_riposte_game_engine_logic_GameViewModel_getBestStepNativeTT(
        JNIEnv *env,
        jobject /* this */,
        jlong p1,
        jlong p2,
        jint p,
        jint d,
        jboolean r,
        jint s,
        jint offW,
        jint defW) {

    MoveData bestMove = Riposte_TT_Engine::getBestStep((uint64_t)p1, (uint64_t)p2, p, d, r, s, (int)offW, (int)defW);

    jclass moveDataClass = env->FindClass("hu/riposte/game/engine/data/MoveData");
    jmethodID constructor = env->GetMethodID(moveDataClass, "<init>", "(III)V");

    return env->NewObject(moveDataClass, constructor, bestMove[0], bestMove[1], bestMove[2]);
}
