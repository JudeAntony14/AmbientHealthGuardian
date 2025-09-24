package com.jude.ambienthealthguardian

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.content.Context
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AlertDialog
import kotlin.random.Random

// Data class for vitals
data class VitalsReading(val heartRate: Int, val spo2: Int, val fall: Boolean)

class MainActivity : AppCompatActivity() {
    // Sample readings
    private val sampleReadings = listOf(
        VitalsReading(82, 98, false),
        VitalsReading(90, 96, false),
        VitalsReading(75, 97, false),
        VitalsReading(42, 84, true)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val testButton = findViewById<MaterialButton>(R.id.manual_test_button)
        val heartRateView = findViewById<TextView>(R.id.heart_rate_value)
        val spo2View = findViewById<TextView>(R.id.spo2_value)
        val motionStatusView = findViewById<TextView>(R.id.motion_status_value)
        val alertMessageView = findViewById<TextView>(R.id.alert_message)

        testButton.setOnClickListener {
            val reading = sampleReadings.random()
            updateVitalsUI(reading, heartRateView, spo2View, motionStatusView)
            handleAlerts(reading, alertMessageView)
        }
    }

    private fun updateVitalsUI(
        reading: VitalsReading,
        heartRateView: TextView,
        spo2View: TextView,
        motionStatusView: TextView
    ) {
        heartRateView.text = "${reading.heartRate} bpm"
        spo2View.text = "${reading.spo2} %"
        motionStatusView.text = if (reading.fall) "Fall Detected" else "--"
    }

    private fun handleAlerts(reading: VitalsReading, alertMessageView: TextView) {
        val isAbnormal = reading.heartRate < 50 || reading.spo2 < 92 || reading.fall
        if (isAbnormal) {
            alertMessageView.text = "Abnormal vitals detected!"
            showSOSDialog()
        } else {
            alertMessageView.text = "No alerts."
        }
    }

    private fun showSOSDialog() {
        AlertDialog.Builder(this)
            .setTitle("SOS Alert")
            .setMessage("Abnormal vitals detected! Call placed to 123.")
            .setPositiveButton("OK", null)
            .show()
    }
}