package com.example.edgeviewer

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import com.example.edgeviewer.utils.ImageUtils
import java.util.concurrent.Semaphore

class CameraController(
    private val context: Context,
    private val frameCallback: (ByteArray, Int, Int) -> Unit
) {
    companion object {
        private const val TAG = "CameraController"
        private const val CAMERA_WIDTH = 1280
        private const val CAMERA_HEIGHT = 720
    }

    private lateinit var cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private lateinit var imageReader: ImageReader

    private val cameraOpenCloseLock = Semaphore(1)
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    init {
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    fun startCamera(surface: Surface) {
        startBackgroundThread()
        openCamera()
    }

    fun stopCamera() {
        closeCamera()
        stopBackgroundThread()
    }

    private fun openCamera() {
        try {
            val cameraId = cameraManager.cameraIdList[0] // Use back camera

            // Setup ImageReader for processing frames
            imageReader = ImageReader.newInstance(
                CAMERA_WIDTH,
                CAMERA_HEIGHT,
                ImageFormat.YUV_420_888,
                2
            )

            imageReader.setOnImageAvailableListener({
                val image = it.acquireLatestImage()
                if (image != null) {
                    processImage(image)
                    image.close()
                }
            }, backgroundHandler)

            if (!cameraOpenCloseLock.tryAcquire(2500, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }

            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open camera", e)
        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice = camera
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraOpenCloseLock.release()
            camera.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            onDisconnected(camera)
        }
    }

    private fun createCameraPreviewSession() {
        try {
            val surface = imageReader.surface

            // Create capture session with ImageReader surface
            cameraDevice?.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        if (cameraDevice == null) return

                        captureSession = session

                        // Setup continuous capture
                        val captureRequest = cameraDevice!!.createCaptureRequest(
                            CameraDevice.TEMPLATE_PREVIEW
                        ).apply {
                            addTarget(surface)
                            set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            set(CaptureRequest.CONTROL_AE_MODE,
                                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                        }

                        session.setRepeatingRequest(
                            captureRequest.build(),
                            null,
                            backgroundHandler
                        )
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "Failed to configure camera session")
                    }
                },
                backgroundHandler
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create preview session", e)
        }
    }

    private fun processImage(image: Image) {
        // Convert YUV to RGB
        val rgbData = ImageUtils.convertYuvToRgb(image)

        // Send to processing callback
        frameCallback(rgbData, image.width, image.height)
    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReader.close()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Interrupted while closing camera", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(TAG, "Interrupted while stopping background thread", e)
        }
    }
}