package opengl

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object ShaderUtils {
    private const val TAG = "ShaderUtils"

    fun loadShaderFromAssets(context: Context, type: Int, fileName: String): Int {
        val shaderCode = readShaderFromAssets(context, fileName)
        return loadShader(type, shaderCode)
    }

    fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)

        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        // Check compilation status
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)

        if (compiled[0] == 0) {
            Log.e(TAG, "Error compiling shader: ${GLES20.glGetShaderInfoLog(shader)}")
            GLES20.glDeleteShader(shader)
            return 0
        }

        return shader
    }

    fun createProgram(vertexShader: Int, fragmentShader: Int): Int {
        val program = GLES20.glCreateProgram()

        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        // Check link status
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)

        if (linkStatus[0] == 0) {
            Log.e(TAG, "Error linking program: ${GLES20.glGetProgramInfoLog(program)}")
            GLES20.glDeleteProgram(program)
            return 0
        }

        return program
    }

    private fun readShaderFromAssets(context: Context, fileName: String): String {
        val stringBuilder = StringBuilder()
        try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append('\n')
            }
            reader.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading shader file: $fileName", e)
        }
        return stringBuilder.toString()
    }
}