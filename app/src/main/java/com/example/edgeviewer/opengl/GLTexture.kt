package opengl

import android.opengl.GLES20.*

class GLTexture {
    private val id = IntArray(1)

    fun create() {
        glGenTextures(1, id, 0)
        glBindTexture(GL_TEXTURE_2D, id[0])
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
    }

    fun update(data: ByteArray, width: Int, height: Int) {
        glBindTexture(GL_TEXTURE_2D, id[0])
        val buffer = java.nio.ByteBuffer.allocateDirect(data.size)
            .apply { put(data); position(0) }
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)
    }

    fun bind(uniformLocation: Int) {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, id[0])
        glUniform1i(uniformLocation, 0)
    }

    fun release() {
        if (id[0] != 0) glDeleteTextures(1, id, 0)
    }
}