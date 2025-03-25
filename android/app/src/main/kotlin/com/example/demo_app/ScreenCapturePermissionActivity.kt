package com.example.demo_app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle

class ScreenCapturePermissionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mgr.createScreenCaptureIntent()
        startActivityForResult(intent, 1001)
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // Start the screen recording service
            val serviceIntent = Intent(this, ScreenRecordingService::class.java)
            serviceIntent.putExtra("resultCode", resultCode)
            serviceIntent.putExtra("data", data)
            startForegroundService(serviceIntent)
        }
        finish() // Close the activity
    }
}
