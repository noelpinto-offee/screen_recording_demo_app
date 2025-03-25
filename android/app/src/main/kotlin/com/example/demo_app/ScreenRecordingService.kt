package com.example.demo_app

import android.annotation.SuppressLint
import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.view.Surface
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.provider.MediaStore
import java.io.File

class ScreenRecordingService : Service() {

    private var mediaProjection: MediaProjection? = null
    private lateinit var mediaRecorder: MediaRecorder
    private var virtualDisplay: VirtualDisplay? = null
    private var isRecordingPaused = false

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            stopRecording()
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: "START_RECORDING"
        when (action) {
            "START_RECORDING" -> startScreenCapture(intent!!)
            "PAUSE_RECORDING" -> pauseRecording()
            "RESUME_RECORDING" -> resumeRecording()
            "STOP_RECORDING" -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun startScreenCapture(intent: Intent) {
        val resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED)
        val data = intent.getParcelableExtra<Intent>("data")

        if (resultCode == Activity.RESULT_OK && data != null) {
            val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)
            mediaProjection?.registerCallback(mediaProjectionCallback, null)

            try {
                mediaRecorder = MediaRecorder().apply {
                    setVideoSource(MediaRecorder.VideoSource.SURFACE)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                    setVideoSize(1280, 720)
                    setVideoFrameRate(30)
                    setOutputFile(getExternalFilesDir(null)?.absolutePath + "/screen_record.mp4")
                    prepare()
                }

                val surface: Surface = mediaRecorder.surface
                virtualDisplay = mediaProjection?.createVirtualDisplay(
                    "ScreenRecording",
                    1280, 720, resources.displayMetrics.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    surface,
                    null, null
                )

                mediaRecorder.start()
                Log.d("ScreenRecordingService", "Screen recording started")
            } catch (e: Exception) {
                Log.e("ScreenRecordingService", "Error starting screen recording: ${e.message}")
                stopSelf()
            }
        } else {
            stopSelf()
        }
    }

    @SuppressLint("NewApi")
    private fun createNotification(): Notification {
        val channelId = "screen_recording_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Screen Recording", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, ScreenRecordingService::class.java).apply {
            action = "STOP_RECORDING"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_MUTABLE
        )

        return Notification.Builder(this, channelId)
            .setContentTitle("Screen Recording")
            .setContentText("Recording screen...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .addAction(android.R.drawable.ic_delete, "Stop", stopPendingIntent)
            .build()
    }

    @SuppressLint("NewApi")
    private fun pauseRecording() {
        if (::mediaRecorder.isInitialized && !isRecordingPaused) {
            try {
                mediaRecorder.pause()
                isRecordingPaused = true
                Log.d("ScreenRecordingService", "Recording paused")
            } catch (e: Exception) {
                Log.e("ScreenRecordingService", "Error pausing recording: ${e.message}")
            }
        }
    }

    @SuppressLint("NewApi")
    private fun resumeRecording() {
        if (::mediaRecorder.isInitialized && isRecordingPaused) {
            try {
                mediaRecorder.resume()
                isRecordingPaused = false
                Log.d("ScreenRecordingService", "Recording resumed")
            } catch (e: Exception) {
                Log.e("ScreenRecordingService", "Error resuming recording: ${e.message}")
            }
        }
    }

    private fun stopRecording() {
        try {
            if (::mediaRecorder.isInitialized) {
                mediaRecorder.stop()
                mediaRecorder.release()
            }
            virtualDisplay?.release()
            mediaProjection?.stop()
            mediaProjection = null

            Log.d("ScreenRecordingService", "Screen recording stopped")
            saveRecording()
        } catch (e: IllegalStateException) {
            Log.e("ScreenRecordingService", "Failed to stop recording properly: ${e.message}")
        }
    }

    private fun saveRecording() {
        val recordedFile = File(getExternalFilesDir(null), "screen_record.mp4")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, "ScreenRecording_${System.currentTimeMillis()}.mp4")
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
            }

            val resolver = contentResolver
            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let { outputStream ->
                resolver.openOutputStream(outputStream)?.use { recordedFile.inputStream().copyTo(it) }
                recordedFile.delete()
                Log.d("ScreenRecordingService", "Recording saved at: $uri")
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val newFile = File(downloadsDir, "ScreenRecording_${System.currentTimeMillis()}.mp4")
            recordedFile.copyTo(newFile, overwrite = true)
            recordedFile.delete()
            Log.d("ScreenRecordingService", "Recording moved to: ${newFile.absolutePath}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}