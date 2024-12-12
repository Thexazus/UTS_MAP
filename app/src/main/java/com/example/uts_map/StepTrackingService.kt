import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.uts_map.SaveStepsWorker
import java.util.concurrent.TimeUnit

class StepTrackingService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepDetector: Sensor? = null
    private var stepCount = 0
    private var lastStepTimeMillis = 0L

    private val binder = StepTrackingBinder()

    inner class StepTrackingBinder : Binder() {
        fun getService(): StepTrackingService = this@StepTrackingService
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        // Start persistent tracking
        startStepTracking()
    }

    private fun startStepTracking() {
        stepDetector?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)

            // Schedule periodic work to save step count
            val saveStepsWork = PeriodicWorkRequestBuilder<SaveStepsWorker>(15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "save_step_count",
                ExistingPeriodicWorkPolicy.KEEP,
                saveStepsWork
            )
        } ?: Log.w("StepTrackingService", "Step detector sensor not available")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                val currentTimeMillis = System.currentTimeMillis()
                if (currentTimeMillis - lastStepTimeMillis > 300) {
                    stepCount++
                    lastStepTimeMillis = currentTimeMillis

                    // Optional: Broadcast step count update
                    val intent = Intent("STEP_COUNT_UPDATE")
                    intent.putExtra("step_count", stepCount)
                    sendBroadcast(intent)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle sensor accuracy changes
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    fun getStepCount(): Int = stepCount
}