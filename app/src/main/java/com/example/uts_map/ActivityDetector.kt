package com.example.uts_map

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

class ActivityDetector(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepDetector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var stepCount = 0
    private var lastStepTimeMillis = 0L
    private var lastAccelerometerReading: FloatArray? = null
    private var activityIntensity = 0f

    // More robust activity tracking
    private var activityStartTime = 0L
    private var currentActivityType = ActivityType.STATIONARY

    private var onWaterGoalIncreasedListener: OnWaterGoalIncreasedListener? = null

    // More detailed activity types
    private enum class ActivityType {
        STATIONARY, LIGHT_ACTIVITY, WALKING, RUNNING, INTENSE_ACTIVITY
    }

    init {
        if (stepDetector == null || accelerometer == null) {
            Log.w("AdvancedActivityDetector", "Step detector or accelerometer sensor not available")
        }
    }

    fun setOnWaterGoalIncreasedListener(listener: OnWaterGoalIncreasedListener) {
        onWaterGoalIncreasedListener = listener
    }

    fun start() {
        stepDetector?.also { stepSensor ->
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: Log.w("AdvancedActivityDetector", "Cannot start step detector - sensor not available")

        accelerometer?.also { acc ->
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: Log.w("AdvancedActivityDetector", "Cannot start accelerometer - sensor not available")
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        Log.d("AdvancedActivityDetector", "Sensors stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                handleStepDetection(event)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                handleAccelerometerData(event)
            }
        }
    }

    private fun handleStepDetection(event: SensorEvent) {
        val currentTimeMillis = System.currentTimeMillis()

        // More robust step detection
        if (currentTimeMillis - lastStepTimeMillis > 300 &&
            activityIntensity > WALKING_INTENSITY_THRESHOLD) {
            stepCount++
            lastStepTimeMillis = currentTimeMillis

            // Determine activity duration and type
            updateActivityType(currentTimeMillis)

            Log.d("AdvancedActivityDetector", "Step detected: $stepCount")

            // Increase water goal based on activity intensity
            determineWaterIncrease()
        }
    }

    private fun updateActivityType(currentTimeMillis: Long) {
        // If no activity has been tracked yet, start tracking
        if (activityStartTime == 0L) {
            activityStartTime = currentTimeMillis
        }

        // Determine activity type based on duration and intensity
        val activityDuration = currentTimeMillis - activityStartTime
        currentActivityType = when {
            activityIntensity < WALKING_INTENSITY_THRESHOLD -> ActivityType.STATIONARY
            activityIntensity < LIGHT_ACTIVITY_THRESHOLD -> ActivityType.LIGHT_ACTIVITY
            activityIntensity < RUNNING_INTENSITY_THRESHOLD -> ActivityType.WALKING
            activityIntensity < INTENSE_ACTIVITY_THRESHOLD -> ActivityType.RUNNING
            else -> ActivityType.INTENSE_ACTIVITY
        }

        // Reset activity start time if activity changes or after long duration
        if (activityDuration > TimeUnit.MINUTES.toMillis(5)) {
            activityStartTime = currentTimeMillis
        }
    }

    private fun handleAccelerometerData(event: SensorEvent) {
        val currentReading = event.values
        lastAccelerometerReading?.let { last ->
            // Calculate total acceleration magnitude
            val totalAcceleration = calculateAccelerationMagnitude(currentReading)

            // More sophisticated moving average for activity intensity
            activityIntensity = (activityIntensity * 0.8f) + (totalAcceleration * 0.2f)
        }
        lastAccelerometerReading = currentReading
    }

    private fun calculateAccelerationMagnitude(values: FloatArray): Float {
        return sqrt(
            values[0] * values[0] +
                    values[1] * values[1] +
                    values[2] * values[2]
        )
    }

    private fun determineWaterIncrease() {
        val waterIncrement = when (currentActivityType) {
            ActivityType.STATIONARY -> 0
            ActivityType.LIGHT_ACTIVITY -> 25 // 25ml for light activity
            ActivityType.WALKING -> 50 // 50ml for walking
            ActivityType.RUNNING -> 100 // 100ml for running
            ActivityType.INTENSE_ACTIVITY -> 150 // 150ml for intense activity
        }

        if (waterIncrement > 0) {
            try {
                onWaterGoalIncreasedListener?.onWaterGoalIncreased(waterIncrement)
                Log.d("AdvancedActivityDetector", "Increasing water goal by $waterIncrement ml due to $currentActivityType")
            } catch (e: Exception) {
                Log.e("AdvancedActivityDetector", "Error increasing water goal", e)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE ->
                Log.w("AdvancedActivityDetector", "Sensor accuracy is unreliable")
            SensorManager.SENSOR_STATUS_NO_CONTACT ->
                Log.w("AdvancedActivityDetector", "Sensor has no contact")
        }
    }

    interface OnWaterGoalIncreasedListener {
        fun onWaterGoalIncreased(newGoal: Int)
    }

    // Check sensor availability
    fun isSensorAvailable(): Boolean {
        return stepDetector != null && accelerometer != null
    }

    companion object {
        // More refined intensity thresholds
        private const val WALKING_INTENSITY_THRESHOLD = 1.5f
        private const val LIGHT_ACTIVITY_THRESHOLD = 2.0f
        private const val RUNNING_INTENSITY_THRESHOLD = 3.0f
        private const val INTENSE_ACTIVITY_THRESHOLD = 5.0f
    }
}