package com.jude.ambienthealthguardian

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import kotlin.random.Random

// Data class for vitals
data class VitalsReading(val heartRate: Int, val spo2: Int, val fall: Boolean, val motion: Int)

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    // Sample readings
    private val sampleReadings = listOf(
        VitalsReading(75, 98, false, 10), // Normal
        VitalsReading(110, 93, false, 15), // Minor abnormal
        VitalsReading(42, 80, true, 5), // Severe abnormal
        VitalsReading(160, 97, false, 80) // Exercise
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

        prefs = getSharedPreferences("health_guardian_prefs", Context.MODE_PRIVATE)
        setupProfileInputs()
        setupContactsInputs()
        setupVitalsTestLogic()
    }

    private fun setupProfileInputs() {
        val nameInput = findViewById<EditText>(R.id.profile_name_input)
        val ageInput = findViewById<EditText>(R.id.profile_age_input)
        val genderInput = findViewById<EditText>(R.id.profile_gender_input)
        val bloodTypeInput = findViewById<EditText>(R.id.profile_blood_type_input)
        val heightInput = findViewById<EditText>(R.id.profile_height_input)
        val weightInput = findViewById<EditText>(R.id.profile_weight_input)
        val bmiInput = findViewById<EditText>(R.id.profile_bmi_input)
        val allergiesInput = findViewById<EditText>(R.id.profile_allergies_input)
        val conditionsInput = findViewById<EditText>(R.id.profile_conditions_input)
        val saveButton = findViewById<Button>(R.id.profile_save_button)

        // Load saved values
        nameInput.setText(prefs.getString("profile_name", ""))
        ageInput.setText(prefs.getString("profile_age", ""))
        genderInput.setText(prefs.getString("profile_gender", ""))
        bloodTypeInput.setText(prefs.getString("profile_blood_type", ""))
        heightInput.setText(prefs.getString("profile_height", ""))
        weightInput.setText(prefs.getString("profile_weight", ""))
        bmiInput.setText(prefs.getString("profile_bmi", ""))
        allergiesInput.setText(prefs.getString("profile_allergies", ""))
        conditionsInput.setText(prefs.getString("profile_conditions", ""))

        saveButton.setOnClickListener {
            prefs.edit()
                .putString("profile_name", nameInput.text.toString())
                .putString("profile_age", ageInput.text.toString())
                .putString("profile_gender", genderInput.text.toString())
                .putString("profile_blood_type", bloodTypeInput.text.toString())
                .putString("profile_height", heightInput.text.toString())
                .putString("profile_weight", weightInput.text.toString())
                .putString("profile_bmi", bmiInput.text.toString())
                .putString("profile_allergies", allergiesInput.text.toString())
                .putString("profile_conditions", conditionsInput.text.toString())
                .apply()
            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupContactsInputs() {
        val name1 = findViewById<EditText>(R.id.contact_name_1_input)
        val phone1 = findViewById<EditText>(R.id.contact_phone_1_input)
        val relation1 = findViewById<EditText>(R.id.contact_relation_1_input)
        val name2 = findViewById<EditText>(R.id.contact_name_2_input)
        val phone2 = findViewById<EditText>(R.id.contact_phone_2_input)
        val relation2 = findViewById<EditText>(R.id.contact_relation_2_input)
        val saveButton = findViewById<Button>(R.id.contacts_save_button)

        // Load saved values
        name1.setText(prefs.getString("contact_name_1", ""))
        phone1.setText(prefs.getString("contact_phone_1", ""))
        relation1.setText(prefs.getString("contact_relation_1", ""))
        name2.setText(prefs.getString("contact_name_2", ""))
        phone2.setText(prefs.getString("contact_phone_2", ""))
        relation2.setText(prefs.getString("contact_relation_2", ""))

        saveButton.setOnClickListener {
            prefs.edit()
                .putString("contact_name_1", name1.text.toString())
                .putString("contact_phone_1", phone1.text.toString())
                .putString("contact_relation_1", relation1.text.toString())
                .putString("contact_name_2", name2.text.toString())
                .putString("contact_phone_2", phone2.text.toString())
                .putString("contact_relation_2", relation2.text.toString())
                .apply()
            Toast.makeText(this, "Contacts saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupVitalsTestLogic() {
        val testButton = findViewById<MaterialButton>(R.id.manual_test_button)
        val heartRateView = findViewById<TextView>(R.id.heart_rate_value)
        val spo2View = findViewById<TextView>(R.id.spo2_value)
        val motionStatusView = findViewById<TextView>(R.id.motion_status_value)
        val alertMessageView = findViewById<TextView>(R.id.alert_message)
        val motionValueView = findViewById<TextView>(R.id.motion_value)

        testButton.setOnClickListener {
            val reading = sampleReadings.random()
            updateVitalsUI(reading, heartRateView, spo2View, motionStatusView, motionValueView)
            classifyAndAlert(reading, alertMessageView)
        }
    }

    private fun updateVitalsUI(
        reading: VitalsReading,
        heartRateView: TextView,
        spo2View: TextView,
        motionStatusView: TextView,
        motionValueView: TextView
    ) {
        heartRateView.text = "${reading.heartRate} bpm"
        spo2View.text = "${reading.spo2} %"
        motionStatusView.text = if (reading.fall) "Fall Detected" else "--"
        motionValueView.text = "Motion: ${reading.motion}"
    }

    private fun classifyAndAlert(reading: VitalsReading, alertMessageView: TextView) {
        // Classification
        val hr = reading.heartRate
        val spo2 = reading.spo2
        val fall = reading.fall
        val motion = reading.motion
        val isExercise = (hr > 120 && motion > 50)
        val isSevere = (!isExercise && (hr < 50 || hr > 120 || spo2 < 90 || (fall && (hr < 60 || spo2 < 95))))
        val isMinor = (!isExercise && !isSevere && ((hr in 101..120) || (hr in 50..59) || (spo2 in 90..94)))
        val isFallNormal = (fall && hr in 60..100 && spo2 in 95..100)

        when {
            isExercise -> {
                alertMessageView.text = "No alerts. (Exercise detected)"
            }
            isSevere -> {
                alertMessageView.text = "Severe abnormal vitals!"
                showCountdownPopup("We detected a possible emergency. Are you okay?", severe = true)
            }
            isMinor -> {
                alertMessageView.text = "Minor abnormal vitals detected."
                showNotification("Minor abnormal vitals detected. Please check your health.")
            }
            isFallNormal -> {
                alertMessageView.text = "Fall detected, normal vitals."
                showCountdownPopup("We detected a fall. Are you okay?", severe = false)
            }
            else -> {
                alertMessageView.text = "No alerts."
            }
        }
    }

    private fun showCountdownPopup(message: String, severe: Boolean) {
        var secondsLeft = 10
        val dialog = AlertDialog.Builder(this)
            .setTitle("SOS Alert")
            .setMessage("$message\n\nRespond in $secondsLeft seconds or we will notify your emergency contacts.")
            .setCancelable(false)
            .setPositiveButton("Yes") { d, _ -> d.dismiss() }
            .setNegativeButton("No") { d, _ ->
                d.dismiss()
                sendSOS(severe)
            }
            .create()
        dialog.show()
        val handler = Handler(Looper.getMainLooper())
        val timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsLeft = (millisUntilFinished / 1000).toInt()
                dialog.setMessage("$message\n\nRespond in $secondsLeft seconds or we will notify your emergency contacts.")
            }
            override fun onFinish() {
                if (dialog.isShowing) {
                    dialog.dismiss()
                    sendSOS(severe)
                }
            }
        }
        timer.start()
        dialog.setOnDismissListener { timer.cancel() }
    }

    private fun sendSOS(severe: Boolean) {
        val msg = if (severe) {
            "Emergency! Alert sent to emergency services and contacts."
        } else {
            "Alert sent to emergency contacts."
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun showNotification(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}