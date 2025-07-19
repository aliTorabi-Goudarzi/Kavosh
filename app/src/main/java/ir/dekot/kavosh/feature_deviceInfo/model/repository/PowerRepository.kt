package ir.dekot.kavosh.feature_deviceInfo.model.repository

import android.content.Intent
import ir.dekot.kavosh.feature_deviceInfo.model.BatteryInfo
import ir.dekot.kavosh.feature_deviceInfo.model.PowerDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.ThermalInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مخزن اطلاعات برق و انرژی - مسئول مدیریت اطلاعات مربوط به باتری و حرارت
 * شامل وضعیت باتری، اطلاعات شارژ و اطلاعات حرارتی دستگاه
 */
@Singleton
class PowerRepository @Inject constructor(
    private val powerDataSource: PowerDataSource
) {

    /**
     * دریافت اطلاعات باتری از Intent
     * @param intent Intent حاوی اطلاعات باتری از سیستم
     * @return اطلاعات کامل باتری شامل درصد شارژ، وضعیت و دما
     */
    fun getBatteryInfo(intent: Intent): BatteryInfo = powerDataSource.getBatteryInfo(intent)

    /**
     * دریافت اطلاعات اولیه باتری
     * @return اطلاعات باتری یا null در صورت عدم دسترسی
     */
    fun getInitialBatteryInfo(): BatteryInfo? = powerDataSource.getInitialBatteryInfo()

    /**
     * دریافت اطلاعات فعلی باتری
     * @return اطلاعات باتری فعلی یا مقدار پیش‌فرض
     */
    fun getCurrentBatteryInfo(): BatteryInfo {
        return getInitialBatteryInfo() ?: BatteryInfo()
    }


}
