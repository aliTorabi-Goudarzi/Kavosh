package ir.dekot.kavosh.domain.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorHandler @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _sensorState = MutableStateFlow<SensorState>(SensorState.Loading)
    val sensorState = _sensorState.asStateFlow()

    private var currentSensorType: Int = -1

    // متغیرهای مورد نیاز برای محاسبات
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private var rotationVectorHistory = mutableListOf<FloatArray>()
    private var stepEventCounter = 0

    fun startListening(sensorType: Int) {
        stopListening()
        currentSensorType = sensorType
        _sensorState.value = SensorState.Loading

        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor == null) {
            _sensorState.value = SensorState.NotAvailable("Sensor not found on this device.")
            return
        }

        // *** شروع منطق اصلاح شده و نهایی ***

        // اگر سنسور از نوع تریگر است، یک وضعیت اولیه "منتظر" ارسال کن
        if (sensorType == Sensor.TYPE_STEP_DETECTOR || sensorType == Sensor.TYPE_SIGNIFICANT_MOTION) {
            _sensorState.value = SensorState.TriggerEvent(0)
        }
        // اگر سنسور گام‌شمار است، یک مقدار اولیه صفر ارسال کن تا UI از حالت لودینگ خارج شود
        else if (sensorType == Sensor.TYPE_STEP_COUNTER) {
            _sensorState.value = SensorState.SingleValue(0f)
        }

        // *** پایان منطق اصلاح شده ***

        val delay = if (sensorType == Sensor.TYPE_ROTATION_VECTOR)
            SensorManager.SENSOR_DELAY_UI else SensorManager.SENSOR_DELAY_NORMAL

        if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), delay)
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), delay)
        } else {
            sensorManager.registerListener(this, sensor, delay)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        rotationVectorHistory.clear()
        stepEventCounter = 0
        currentSensorType = -1
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (currentSensorType == Sensor.TYPE_MAGNETIC_FIELD) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            }
            updateOrientationState()
            return
        }

        if (event.sensor.type != currentSensorType) return

        _sensorState.value = when (event.sensor.type) {
            Sensor.TYPE_LIGHT, Sensor.TYPE_PRESSURE, Sensor.TYPE_AMBIENT_TEMPERATURE, Sensor.TYPE_RELATIVE_HUMIDITY, Sensor.TYPE_PROXIMITY, Sensor.TYPE_STEP_COUNTER ->
                SensorState.SingleValue(event.values[0])

            Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_GRAVITY, Sensor.TYPE_LINEAR_ACCELERATION ->
                SensorState.VectorValue(event.values[0], event.values[1], event.values[2])

            Sensor.TYPE_ACCELEROMETER_UNCALIBRATED, Sensor.TYPE_GYROSCOPE_UNCALIBRATED, Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED ->
                SensorState.UncalibratedVectorValue(event.values[0], event.values[1], event.values[2], event.values[3], event.values[4], event.values[5])

            Sensor.TYPE_ROTATION_VECTOR -> {
                rotationVectorHistory.add(event.values.clone())
                if (rotationVectorHistory.size > 100) rotationVectorHistory.removeAt(0)
                SensorState.RotationVectorState(rotationVectorHistory.toList())
            }

            Sensor.TYPE_STEP_DETECTOR, Sensor.TYPE_SIGNIFICANT_MOTION -> {
                stepEventCounter++
                sensorManager.registerListener(this, event.sensor, SensorManager.SENSOR_DELAY_NORMAL)
                SensorState.TriggerEvent(stepEventCounter)
            }

            else -> SensorState.NotAvailable("Live view is not supported for this sensor.")
        }
    }

    private fun updateOrientationState() {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        val orientationAngles = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        _sensorState.value = SensorState.CompassState(
            orientationAngles = orientationAngles,
            accelerometerData = accelerometerReading.clone(),
            magnetometerData = magnetometerReading.clone()
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
}