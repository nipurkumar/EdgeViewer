package opengl


import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import opengl.ShaderUtils

class GLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private var program = 0
    private var texture: GLTexture? = null

    // Geometry (full‑screen quad)
    private val vertexCoords = floatArrayOf(
        -1f,  1f, 0f,
        -1f, -1f, 0f,
        1f, -1f, 0f,
        1f,  1f, 0f
    )
    private val texCoords = floatArrayOf(
        0f, 0f,
        0f, 1f,
        1f, 1f,
        1f, 0f
    )
    private val indices = shortArrayOf(0, 1, 2, 0, 2, 3)

    // Buffers (created once)
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var texCoordBuffer: FloatBuffer
    private lateinit var indexBuffer: ShortBuffer

    // Texture update queue
    @Volatile private var pendingData: ByteArray? = null
    @Volatile private var pendingW = 0
    @Volatile private var pendingH = 0
    @Volatile private var needsUpdate = false

    // -----------------------------------------------------------------
    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        glClearColor(0f, 0f, 0f, 1f)

        // ---- 1. Shader program ------------------------------------
        program = ShaderUtils.loadProgramFromAssets(
            context,
            "vertex_shader.glsl",
            "fragment_shader.glsl"
        )
        glUseProgram(program)

        // ---- 2. Geometry buffers ----------------------------------
        vertexBuffer = floatBuffer(vertexCoords)
        texCoordBuffer = floatBuffer(texCoords)
        indexBuffer = shortBuffer(indices)

        // ---- 3. Texture -------------------------------------------
        texture = GLTexture()
        texture?.create()

        // ---- 4. Blend (optional) ----------------------------------
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(unused: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)

        // ---- Update texture if new frame -------------------------
        if (needsUpdate && pendingData != null) {
            texture?.update(pendingData!!, pendingW, pendingH)
            needsUpdate = false
            pendingData = null
        }

        // ---- Draw ------------------------------------------------
        glUseProgram(program)

        val posLoc = glGetAttribLocation(program, "aPosition")
        val texLoc = glGetAttribLocation(program, "aTexCoord")
        val texUni = glGetUniformLocation(program, "uTexture")

        glEnableVertexAttribArray(posLoc)
        glEnableVertexAttribArray(texLoc)

        vertexBuffer.position(0)
        glVertexAttribPointer(posLoc, 3, GL_FLOAT, false, 0, vertexBuffer)

        texCoordBuffer.position(0)
        glVertexAttribPointer(texLoc, 2, GL_FLOAT, false, 0, texCoordBuffer)

        texture?.bind(texUni)

        indexBuffer.position(0)
        glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_SHORT, indexBuffer)

        glDisableVertexAttribArray(posLoc)
        glDisableVertexAttribArray(texLoc)
    }

    // -----------------------------------------------------------------
    fun updateTexture(data: ByteArray, w: Int, h: Int) {
        pendingData = data.copyOf()
        pendingW = w
        pendingH = h
        needsUpdate = true
    }

    fun release() {
        texture?.release()
        if (program != 0) glDeleteProgram(program)
    }

    // -----------------------------------------------------------------
    private fun floatBuffer(arr: FloatArray): FloatBuffer =
        ByteBuffer.allocateDirect(arr.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(arr); position(0) }

    private fun shortBuffer(arr: ShortArray): ShortBuffer =
        ByteBuffer.allocateDirect(arr.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply { put(arr); position(0) }
}