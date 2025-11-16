package opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private var texture: GLTexture? = null
    private var shaderProgram: Int = 0
    private var vertexBuffer: ByteBuffer? = null

    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    // Vertex coordinates for full-screen quad
    private val vertexCoords = floatArrayOf(
        -1.0f,  1.0f, 0.0f,  // top left
        -1.0f, -1.0f, 0.0f,  // bottom left
        1.0f, -1.0f, 0.0f,  // bottom right
        1.0f,  1.0f, 0.0f   // top right
    )

    // Texture coordinates
    private val textureCoords = floatArrayOf(
        0.0f, 0.0f,  // top left
        0.0f, 1.0f,  // bottom left
        1.0f, 1.0f,  // bottom right
        1.0f, 0.0f   // top right
    )

    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)

    private var textureData: ByteArray? = null
    private var textureWidth = 0
    private var textureHeight = 0
    private var textureUpdated = false

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // Initialize shader program
        initializeShaders()

        // Initialize vertex buffer
        initializeBuffers()

        // Initialize texture
        texture = GLTexture()

        // Enable blend for transparency
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Update texture if new data is available
        if (textureUpdated && textureData != null) {
            texture?.updateTexture(textureData!!, textureWidth, textureHeight)
            textureUpdated = false
        }

        // Use shader program
        GLES20.glUseProgram(shaderProgram)

        // Set matrices
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Get shader locations
        val positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition")
        val texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "aTexCoord")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        val textureHandle = GLES20.glGetUniformLocation(shaderProgram, "uTexture")

        // Enable vertex attribute
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)

        // Set vertex data
        vertexBuffer?.position(0)
        GLES20.glVertexAttribPointer(
            positionHandle, 3, GLES20.GL_FLOAT, false,
            0, vertexBuffer
        )

        // Set texture coordinate data
        val texBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureCoords)
            .position(0)

        GLES20.glVertexAttribPointer(
            texCoordHandle, 2, GLES20.GL_FLOAT, false,
            0, texBuffer
        )

        // Set matrices
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // Bind texture
        texture?.bind(textureHandle)

        // Draw quad
        val indexBuffer = ByteBuffer.allocateDirect(drawOrder.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(drawOrder)
            .position(0)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, drawOrder.size,
            GLES20.GL_UNSIGNED_SHORT, indexBuffer
        )

        // Disable vertex attributes
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun initializeShaders() {
        val vertexShader = ShaderUtils.loadShaderFromAssets(
            context, GLES20.GL_VERTEX_SHADER, "vertex_shader.glsl"
        )
        val fragmentShader = ShaderUtils.loadShaderFromAssets(
            context, GLES20.GL_FRAGMENT_SHADER, "fragment_shader.glsl"
        )

        shaderProgram = ShaderUtils.createProgram(vertexShader, fragmentShader)
    }

    private fun initializeBuffers() {
        vertexBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexCoords)
            .apply { position(0) } as ByteBuffer
    }

    fun updateTexture(data: ByteArray, width: Int, height: Int) {
        textureData = data
        textureWidth = width
        textureHeight = height
        textureUpdated = true
    }

    fun release() {
        texture?.release()
        GLES20.glDeleteProgram(shaderProgram)
    }
}