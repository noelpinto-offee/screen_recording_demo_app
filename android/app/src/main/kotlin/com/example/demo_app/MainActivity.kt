package com.example.demo_app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.content.pm.PackageManager
import android.os.Build
import android.net.Uri

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.example.demo_app/native"
    private val SCREEN_CAPTURE_REQUEST_CODE = 1001

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startRecording" -> {
                    val intent = Intent(this, ScreenCapturePermissionActivity::class.java)
                    startActivityForResult(intent, SCREEN_CAPTURE_REQUEST_CODE)
                    result.success("Started")
                }
                "stopRecording" -> {
                    val intent = Intent(this, ScreenRecordingService::class.java)
                    stopService(intent)
                    result.success("Stopped")
                }
                "pauseRecording" -> {
                    val intent = Intent(this, ScreenRecordingService::class.java)
                    intent.action = "PAUSE_RECORDING"
                    startService(intent)
                    result.success("Paused")
                }
                "resumeRecording" -> {
                    val intent = Intent(this, ScreenRecordingService::class.java)
                    intent.action = "RESUME_RECORDING"
                    startService(intent)
                    result.success("Resumed")
                }
                else -> result.notImplemented()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val serviceIntent = Intent(this, ScreenRecordingService::class.java)
            serviceIntent.putExtra("resultCode", resultCode)
            serviceIntent.putExtra("data", data)
            startService(serviceIntent)
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )

        val needsManageStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            !Environment.isExternalStorageManager()
        } else false

        return true
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 1)

        // Android 11+ requires special handling for MANAGE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        }
    }
}
