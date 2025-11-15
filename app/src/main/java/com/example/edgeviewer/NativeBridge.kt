package com.example.edgeviewer

/**
 * JNI Bridge to Native C++ OpenCV processing
 */
class NativeBridge {

    external fun initializeNative(): Boolean

    external fun processFrame(
        rgbData: ByteArray,
        width: Int,
        height: Int
    ): ByteArray

    external fun setProcessingMode(mode: Int)

    external fun updateProcessingTime(timeMs: Double)

    external fun getNativeFps(): Double

    external fun release()
}