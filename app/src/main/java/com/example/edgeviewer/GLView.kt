package com.example.edgeviewer

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceHolder
import opengl.GLRenderer


class GLView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val renderer = GLRenderer(context)

    internal val glRenderer get() = renderer

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
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

    val surface: Surface?
        get() = holder?.surface

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        super.surfaceDestroyed(holder)
        queueEvent { renderer.release() }
    }
}