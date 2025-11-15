package com.example.edgeviewer.utils

import android.media.Image
import java.nio.ByteBuffer

object ImageUtils {

    fun convertYuvToRgb(image: Image): ByteArray {
        val planes = image.planes
        val yPlane = planes[0]
        val uPlane = planes[1]
        val vPlane = planes[2]

        val ySize = yPlane.buffer.remaining()
        val uSize = uPlane.buffer.remaining()
        val vSize = vPlane.buffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yPlane.buffer.get(nv21, 0, ySize)
        vPlane.buffer.get(nv21, ySize, vSize)
        uPlane.buffer.get(nv21, ySize + vSize, uSize)

        return yuvToRgb(nv21, image.width, image.height)
    }

    private fun yuvToRgb(yuv: ByteArray, width: Int, height: Int): ByteArray {
        val rgb = ByteArray(width * height * 3)
        val frameSize = width * height

        var r: Int
        var g: Int
        var b: Int
        var y: Int
        var u: Int
        var v: Int

        for (j in 0 until height) {
            for (i in 0 until width) {
                y = yuv[j * width + i].toInt() and 0xff
                u = yuv[frameSize + (j shr 1) * width + (i and 1.inv())].toInt() and 0xff
                v = yuv[frameSize + (j shr 1) * width + (i and 1.inv()) + 1].toInt() and 0xff

                u -= 128
                v -= 128

                r = y + (1.370705f * v).toInt()
                g = y - (0.337633f * u).toInt() - (0.698001f * v).toInt()
                b = y + (1.732446f * u).toInt()

                r = r.coerceIn(0, 255)
                g = g.coerceIn(0, 255)
                b = b.coerceIn(0, 255)

                val idx = (j * width + i) * 3
                rgb[idx] = r.toByte()
                rgb[idx + 1] = g.toByte()
                rgb[idx + 2] = b.toByte()
            }
        }

        return rgb
    }
}