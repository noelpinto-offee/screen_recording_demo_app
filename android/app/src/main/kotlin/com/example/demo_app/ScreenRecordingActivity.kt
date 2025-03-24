package com.example.demo_app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import dev.bmcreations.scrcast.ScrCast
import dev.bmcreations.scrcast.recorder.RecordingCallbacks
import dev.bmcreations.scrcast.recorder.RecordingState
import java.io.File

class ScreenRecordActivity : ComponentActivity() {
    private lateinit var recorder: ScrCast

    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    recorder = ScrCast.use(this).apply {
            options {
                video {
                    maxLengthSecs = 300
                }
                storage {
                    directoryName = "screen-records"
                }
                notification {
                    showPause = true
                    showStop = true
                    showTimer = true
                    channel {
                        id = "rec"
                        name = "Screen Recording"
                    }
                }
            }
        }

        // Give ScrCast time to bind and set up notificationProvider
        Handler(Looper.getMainLooper()).postDelayed({
            recorder.record()
        }, 500)
    }


    companion object {
        var recorderInstance: ScrCast? = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't stop recording here unless you want to auto-stop
        // Flutter can call recorderInstance?.stopRecording()
        // recorderInstance = recorder
    }
}
