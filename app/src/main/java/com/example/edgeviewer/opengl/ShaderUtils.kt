package opengl

import android.content.Context
import android.opengl.GLES20.*
import java.io.BufferedReader
import java.io.InputStreamReader

/** Loads, compiles and links a vertex + fragment shader from assets. */
object ShaderUtils {

    /** Public API used by GLRenderer */
    fun loadProgramFromAssets(
        context: Context,
        vertexFile: String,
        fragmentFile: String
    ): Int {
        val v = compileShader(context, GL_VERTEX_SHADER, vertexFile)
        val f = compileShader(context, GL_FRAGMENT_SHADER, fragmentFile)
        return linkProgram(v, f)
    }

    // --------------------------------------------------------------
    private fun compileShader(ctx: Context, type: Int, assetName: String): Int {
        val source = readAsset(ctx, assetName)
        val shader = glCreateShader(type)
        glShaderSource(shader, source)
        glCompileShader(shader)

        val ok = IntArray(1)
        glGetShaderiv(shader, GL_COMPILE_STATUS, ok, 0)
        if (ok[0] == 0) {
            val log = glGetShaderInfoLog(shader)
            glDeleteShader(shader)
            throw RuntimeException("Shader compile failed ($assetName):\n$log")
        }
        return shader
    }

    private fun linkProgram(v: Int, f: Int): Int {
        val prog = glCreateProgram()
        glAttachShader(prog, v)
        glAttachShader(prog, f)
        glLinkProgram(prog)

        val ok = IntArray(1)
        glGetProgramiv(prog, GL_LINK_STATUS, ok, 0)
        if (ok[0] == 0) {
            val log = glGetProgramInfoLog(prog)
            glDeleteProgram(prog)
            throw RuntimeException("Program link failed:\n$log")
        }
        return prog
    }

    private fun readAsset(ctx: Context, file: String): String =
        ctx.assets.open(file).use { input ->
            BufferedReader(InputStreamReader(input)).readText()
        }
}