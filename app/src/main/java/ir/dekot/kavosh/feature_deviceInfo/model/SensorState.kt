package ir.dekot.kavosh.feature_deviceInfo.model

/**
 * یک کلاس مهر و موم شده (sealed class) برای مدل‌سازی انواع مختلف وضعیت‌های سنسور.
 * این کار به ما اجازه می‌دهد تا در UI به شکلی امن (type-safe) با داده‌های هر سنسور کار کنیم.
 */
sealed class SensorState {
    /** وضعیت خالی یا اولیه. */
    object Loading : SensorState()

    /** برای سنسورهایی که فقط یک مقدار عددی دارند (مثل نور، فشار، دما). */
    data class SingleValue(val value: Float) : SensorState()

    /** برای سنسورهایی که یک وکتور سه‌بعدی دارند (مثل شتاب‌سنج، ژیروسکوپ). */
    data class VectorValue(val x: Float, val y: Float, val z: Float) : SensorState()

    /** برای سنسورهای کالیبره نشده که داده خام و بایاس دارند. */
    data class UncalibratedVectorValue(
        val x: Float, val y: Float, val z: Float,
        val xBias: Float, val yBias: Float, val zBias: Float
    ) : SensorState()

    /** وضعیت اختصاصی برای قطب‌نما که شامل زوایا و داده‌های خام است. */
    data class CompassState(
        val orientationAngles: FloatArray,
        val accelerometerData: FloatArray,
        val magnetometerData: FloatArray
    ) : SensorState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CompassState

            if (!orientationAngles.contentEquals(other.orientationAngles)) return false
            if (!accelerometerData.contentEquals(other.accelerometerData)) return false
            if (!magnetometerData.contentEquals(other.magnetometerData)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = orientationAngles.contentHashCode()
            result = 31 * result + accelerometerData.contentHashCode()
            result = 31 * result + magnetometerData.contentHashCode()
            return result
        }
    }

    /** وضعیت اختصاصی برای وکتور چرخش که تاریخچه مقادیر را برای نمودار نگه می‌دارد. */
    data class RotationVectorState(val history: List<FloatArray>) : SensorState()

    /** برای سنسورهای رویدادی که فقط یک بار فعال می‌شوند (مثل تشخیص گام). */
    data class TriggerEvent(val eventCount: Int) : SensorState()

    /** وضعیتی برای زمانی که نمایش زنده ممکن نیست. */
    data class NotAvailable(val message: String) : SensorState()
}