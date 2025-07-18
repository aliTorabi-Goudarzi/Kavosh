package ir.dekot.kavosh.feature_deviceInfo.model.repository

import android.os.Build
import androidx.annotation.RequiresApi
import ir.dekot.kavosh.feature_deviceInfo.model.NetworkDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.NetworkInfo
import ir.dekot.kavosh.feature_deviceInfo.model.SimDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.SimInfo
import ir.dekot.kavosh.feature_deviceInfo.model.WifiScanResult
import ir.dekot.kavosh.feature_testing.model.NetworkToolsDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مخزن اطلاعات اتصالات - مسئول مدیریت اطلاعات مربوط به شبکه و ارتباطات
 * شامل اطلاعات شبکه، WiFi، سیم‌کارت و ابزارهای شبکه
 */
@Singleton
class ConnectivityRepository @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val simDataSource: SimDataSource,
    private val networkToolsDataSource: NetworkToolsDataSource
) {

    /**
     * دریافت اطلاعات شبکه
     * @return اطلاعات کامل شبکه شامل نوع اتصال، IP و سرعت
     */
    fun getNetworkInfo(): NetworkInfo = networkDataSource.getNetworkInfo()

    /**
     * دریافت اطلاعات سیم‌کارت‌ها
     * @return لیست اطلاعات سیم‌کارت‌های موجود در دستگاه
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getSimInfo(): List<SimInfo> = simDataSource.getSimInfo()

    /**
     * اسکن شبکه‌های WiFi موجود
     * @return لیست شبکه‌های WiFi یافت شده
     */
    suspend fun getWifiScanResults(): List<WifiScanResult> {
        return networkToolsDataSource.scanForWifiNetworks().map {
            @Suppress("DEPRECATION")
            WifiScanResult(
                ssid = it.SSID.ifEmpty { "(Hidden Network)" },
                bssid = it.BSSID,
                capabilities = it.capabilities,
                level = it.level,
                frequency = it.frequency
            )
        }
    }

    /**
     * پینگ کردن یک هاست
     * @param host آدرس هاست مورد نظر
     * @return Flow حاوی نتایج پینگ
     */
    fun pingHost(host: String): Flow<String> = networkToolsDataSource.pingHost(host)
}
