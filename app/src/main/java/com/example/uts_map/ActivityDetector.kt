package com.example.uts_map

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class ActivityDetector(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepDetector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private var stepCount = 0
    private var lastStepTimeMillis = 0L

    private var onWaterGoalIncreasedListener: OnWaterGoalIncreasedListener? = null

    init {
        if (stepDetector == null) {
            Log.w("ActivityDetector", "Step detector sensor not available")
        }
    }

    fun setOnWaterGoalIncreasedListener(listener: OnWaterGoalIncreasedListener) {
        onWaterGoalIncreasedListener = listener
    }

    fun start() {
        stepDetector?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("ActivityDetector", "Step detector started")
        } ?: Log.w("ActivityDetector", "Cannot start step detector - sensor not available")
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        Log.d("ActivityDetector", "Step detector stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                // Only increment if enough time has passed since last step
                val currentTimeMillis = System.currentTimeMillis()
                if (currentTimeMillis - lastStepTimeMillis > 300) { // Prevent over-counting
                    stepCount++
                    lastStepTimeMillis = currentTimeMillis

                    Log.d("ActivityDetector", "Step detected: $stepCount")

                    // Increase water goal every 10 steps
                    if (stepCount % 10 == 0) {
                        increaseWaterGoal()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE ->
                Log.w("ActivityDetector", "Step sensor accuracy is unreliable")
            SensorManager.SENSOR_STATUS_NO_CONTACT ->
                Log.w("ActivityDetector", "Step sensor has no contact")
        }
    }

    private fun increaseWaterGoal() {
        try {
            // Increase goal by 100ml for every 10 steps
            onWaterGoalIncreasedListener?.onWaterGoalIncreased(100)
            Log.d("ActivityDetector", "Increasing water goal by 100ml")
        } catch (e: Exception) {
            Log.e("ActivityDetector", "Error increasing water goal", e)
        }
    }

    interface OnWaterGoalIncreasedListener {
        fun onWaterGoalIncreased(newGoal: Int)
    }

    // Check sensor availability
    fun isSensorAvailable(): Boolean {
        return stepDetector != null
    }
}