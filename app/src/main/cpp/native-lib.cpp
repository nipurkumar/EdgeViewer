#include <jni.h>
#include <string>
#include <vector>
#include <chrono>
#include <android/log.h>
#include "edge_processor.h"

#define LOG_TAG "NativeLib"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace std::chrono;

// Global edge processor instance
static EdgeProcessor* g_processor = nullptr;
static float g_nativeFps = 0.0f;

extern "C" {

JNIEXPORT jbyteArray JNICALL
Java_com_example_edgeviewer_NativeBridge_processEdgeDetection(
        JNIEnv* env,
        jobject /* this */,
        jbyteArray imageData,
        jint width,
        jint height,
        jint mode) {

    auto start = high_resolution_clock::now();

    // Get input data
    jsize len = env->GetArrayLength(imageData);
    jbyte* input = env->GetByteArrayElements(imageData, nullptr);

    if (input == nullptr) {
        LOGE("Failed to get input array");
        return nullptr;
    }

    // Initialize processor if needed
    if (g_processor == nullptr) {
        g_processor = new EdgeProcessor();
    }

    // Process frame
    std::vector<uint8_t> output;
    bool success = g_processor->processFrame(
            reinterpret_cast<uint8_t*>(input),
            width,
            height,
            static_cast<ProcessingMode>(mode),
            output
    );

    // Release input
    env->ReleaseByteArrayElements(imageData, input, JNI_ABORT);

    if (!success) {
        LOGE("Failed to process frame");
        return nullptr;
    }

    // Create output array
    jbyteArray result = env->NewByteArray(output.size());
    if (result != nullptr) {
        env->SetByteArrayRegion(result, 0, output.size(),
                                reinterpret_cast<const jbyte*>(output.data()));
    }

    // Calculate FPS
    auto end = high_resolution_clock::now();
    auto duration = duration_cast<milliseconds>(end - start);
    if (duration.count() > 0) {
        g_nativeFps = 1000.0f / duration.count();
    }

    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_example_edgeviewer_NativeBridge_initialize(
        JNIEnv* env,
        jobject /* this */) {

    try {
        if (g_processor == nullptr) {
            g_processor = new EdgeProcessor();
        }
        return JNI_TRUE;
    } catch (...) {
        LOGE("Failed to initialize cpp processor");
        return JNI_FALSE;
    }
}

JNIEXPORT void JNICALL
Java_com_example_edgeviewer_NativeBridge_release(
        JNIEnv* env,
jobject /* this */) {

if (g_processor != nullptr) {
delete g_processor;
g_processor = nullptr;
}
}

JNIEXPORT jfloat JNICALL
Java_com_example_edgeviewer_NativeBridge_getNativeFps(
        JNIEnv* env,
        jobject /* this */) {
    return g_nativeFps;
}

}