package com.example.edgeviewer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Surface
import android.view.View
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
        init {
            System.loadLibrary("native-lib")
        }
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

        glView = findViewById(R.id.glView)
        modeButton = findViewById(R.id.modeButton)
        fpsTextView = findViewById(R.id.fpsText)

        // Initialize camera controller with callback
        cameraController = CameraController(this) { imageData, width, height ->
            processFrame(imageData, width, height)
        }

        // Mode toggle button
        modeButton.setOnClickListener {
            currentMode = when (currentMode) {
                Mode.CANNY -> Mode.SOBEL
                Mode.SOBEL -> Mode.GRAYSCALE
                Mode.GRAYSCALE -> Mode.CANNY
            }
            updateModeText()
        }

        // Check camera permissions
        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        // Start FPS counter
        startFpsCounter()
    }

    private fun processFrame(imageData: ByteArray, width: Int, height: Int) {
        frameCount++

        // Process in native code
        val processedData = nativeBridge.processEdgeDetection(
            imageData,
            width,
            height,
            currentMode.value
        )

        // Update OpenGL texture
        glView.updateTexture(processedData, width, height)
    }

    private fun startFpsCounter() {
        fpsTimer.schedule(object : TimerTask() {
            override fun run() {
                val now = System.currentTimeMillis()
                val elapsed = (now - lastFpsTime) / 1000.0
                val fps = frameCount / elapsed

                runOnUiThread {
                    fpsTextView.text = String.format("FPS: %.1f", fps)
                }

                frameCount = 0
                lastFpsTime = now
            }
        }, 1000, 1000)
    }

    private fun updateModeText() {
        modeButton.text = "Mode: ${currentMode.name}"
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    private fun startCamera() {
        cameraController.startCamera(glView.surface)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fpsTimer.cancel()
        cameraController.stopCamera()
        nativeBridge.release()
    }
}