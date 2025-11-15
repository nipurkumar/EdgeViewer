package com.example.edgeviewer

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.SurfaceHolder
import opengl.GLRenderer

class GLView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private lateinit var renderer: GLRenderer
    val surface: android.view.Surface
        get() = holder.surface

    init {
        // Configure OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        // Setup renderer
        renderer = GLRenderer(context)
        setRenderer(renderer)

        // Render only when requested
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun updateTexture(data: ByteArray, width: Int, height: Int) {
        queueEvent {
            renderer.updateTexture(data, width, height)
            requestRender()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        super.surfaceCreated(holder)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        super.surfaceDestroyed(holder)
        renderer.release()
    }
}