package com.example.edgeviewer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.edgeviewer.enums.Mode
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 200
        private const val TAG = "MainActivity"

        init { System.loadLibrary("native-lib") }
    }

    private lateinit var glView: GLView
    private lateinit var cameraController: CameraController
    private lateinit var modeButton: Button
    private lateinit var fpsTextView: TextView
    private val nativeBridge = NativeBridge()

    private var currentMode = Mode.CANNY
    private val fpsTimer = Timer()
    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. UI setup
        glView = findViewById(R.id.glView)
        modeButton = findViewById(R.id.modeButton)
        fpsTextView = findViewById(R.id.fpsText)

        // 2. Native init
        nativeBridge.initialize()

        // 3. Camera → GL pipeline
        cameraController = CameraController(this) { data, w, h ->
            processFrame(data, w, h)
        }

        // 4. TEST TEXTURE – RED SCREEN AFTER 3 SECONDS
        glView.postDelayed({
            val w = 1280
            val h = 720
            val test = ByteArray(w * h * 4).apply {
                for (i in indices step 4) {
                    this[i]     = 255.toByte()   // R
                    this[i + 1] = 0.toByte()     // G
                    this[i + 2] = 0.toByte()     // B
                    this[i + 3] = 255.toByte()   // A
                }
            }
            glView.updateTexture(test, w, h)
            Log.d(TAG, "TEST: Red texture sent")
        }, 3000)

        // 5. Mode button
        modeButton.setOnClickListener {
            currentMode = when (currentMode) {
                Mode.CANNY -> Mode.SOBEL
                Mode.SOBEL -> Mode.GRAYSCALE
                Mode.GRAYSCALE -> Mode.CANNY
            }
            updateModeText()
        }

        // 6. Permission & camera start
        if (checkCameraPermission()) {
            startCameraWhenSurfaceReady()
        } else {
            requestCameraPermission()
        }

        startFpsCounter()
    }

    // -----------------------------------------------------------------
    private fun processFrame(imageData: ByteArray, width: Int, height: Int) {
        frameCount++
        Log.d(TAG, "Frame received: ${width}x$height, size=${imageData.size}")

        val processed = nativeBridge.processEdgeDetection(
            imageData, width, height, currentMode.value
        )

        Log.d(TAG, "Processed frame size: ${processed.size}")

        glView.updateTexture(processed, width, height)
    }

    // -----------------------------------------------------------------
    private fun startFpsCounter() {
        fpsTimer.schedule(object : TimerTask() {
            override fun run() {
                val now = System.currentTimeMillis()
                val elapsed = (now - lastFpsTime) / 1000.0
                val fps = if (elapsed > 0) frameCount / elapsed else 0.0
                runOnUiThread { fpsTextView.text = String.format("FPS: %.1f", fps) }
                frameCount = 0
                lastFpsTime = now
            }
        }, 1000, 1000)
    }

    private fun updateModeText() {
        modeButton.text = "Mode: ${currentMode.name}"
    }

    // -----------------------------------------------------------------
    private fun checkCameraPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    // -----------------------------------------------------------------
    /** Wait for GLView surface → then open camera */
    private fun startCameraWhenSurfaceReady() {
        if (glView.holder.surface?.isValid == true) {
            Log.d(TAG, "Surface ready – opening camera")
            cameraController.startCamera()
        } else {
            Log.w(TAG, "Surface not ready – waiting...")
            glView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    Log.d(TAG, "Surface created – opening camera")
                    cameraController.startCamera()

                    // Remove this callback after first use
                    glView.holder.removeCallback(this)  // 'this' correctly refers to this Callback object
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                override fun surfaceDestroyed(holder: SurfaceHolder) {}
            })

        }
    }

    // -----------------------------------------------------------------
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCameraWhenSurfaceReady()
        }
    }

    // -----------------------------------------------------------------
    override fun onDestroy() {
        super.onDestroy()
        fpsTimer.cancel()
        cameraController.stopCamera()
        nativeBridge.release()
    }
}
