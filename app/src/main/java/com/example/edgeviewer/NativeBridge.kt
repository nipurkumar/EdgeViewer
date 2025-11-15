package com.example.edgeviewer

class NativeBridge {

    external fun processEdgeDetection(
        imageData: ByteArray,
        width: Int,
        height: Int,
        mode: Int
    ): ByteArray

    external fun initialize(): Boolean

    external fun release()

    external fun getNativeFps(): Float
}