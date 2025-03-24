package com.example.demo_app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import java.io.File
import java.io.IOException

object RecordingController {
    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var surface: Surface? = null

    private var isRecording = false
    private var isPaused = false

    private var outputFilePath: String = ""

    private var displayWidth = 720
    private var displayHeight = 1280
    private var screenDensity = 1

    private var projectionManager: MediaProjectionManager? = null

    fun start(context: Context, data: Intent, resultCode: Int) {
        if (isRecording) {
            Toast.makeText(context, "Already recording!", Toast.LENGTH_SHORT).show()
            return
        }

        // Get display metrics
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        displayWidth = metrics.widthPixels
        displayHeight = metrics.heightPixels
        screenDensity = metrics.densityDpi

        // Set output path
        outputFilePath =
            "${context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)}/recording_${System.currentTimeMillis()}.mp4"

        // Setup MediaRecorder
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFilePath)
            setVideoSize(displayWidth, displayHeight)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoEncodingBitRate(5 * 1024 * 1024)
            setVideoFrameRate(30)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e("RecordingController", "MediaRecorder prepare failed", e)
                return
            }
        }

        projectionManager =
            context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager?.getMediaProjection(resultCode, data)

        surface = mediaRecorder?.surface

        mediaProjection?.createVirtualDisplay(
            "ScreenRecorder",
            displayWidth,
            displayHeight,
            screenDensity,
            0,
            surface,
            null,
            null
        )

        mediaRecorder?.start()
        isRecording = true
        isPaused = false

        Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
    }

    fun stop(context: Context) {
        if (!isRecording) return

        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Log.e("RecordingController", "Error stopping recorder", e)
        }

        mediaRecorder?.reset()
        mediaRecorder?.release()
        mediaRecorder = null
        surface = null

        mediaProjection?.stop()
        mediaProjection = null

        isRecording = false
        isPaused = false

        Toast.makeText(context, "Recording saved to: $outputFilePath", Toast.LENGTH_LONG).show()
    }

    fun pause(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isRecording && !isPaused) {
            mediaRecorder?.pause()
            isPaused = true
            Toast.makeText(context, "Recording paused", Toast.LENGTH_SHORT).show()
        }
    }

    fun resume(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isRecording && isPaused) {
            mediaRecorder?.resume()
            isPaused = false
            Toast.makeText(context, "Recording resumed", Toast.LENGTH_SHORT).show()
        }
    }
}
