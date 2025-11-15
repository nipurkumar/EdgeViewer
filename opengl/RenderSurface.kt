package opengl

import android.graphics.SurfaceTexture
import android.view.Surface

class RenderSurface(width: Int, height: Int) {
    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null

    init {
        // Create off-screen surface for rendering
        surfaceTexture = SurfaceTexture(0).apply {
            setDefaultBufferSize(width, height)
        }
        surface = Surface(surfaceTexture)
    }

    fun getSurface(): Surface? = surface

    fun release() {
        surface?.release()
        surfaceTexture?.release()
    }
}