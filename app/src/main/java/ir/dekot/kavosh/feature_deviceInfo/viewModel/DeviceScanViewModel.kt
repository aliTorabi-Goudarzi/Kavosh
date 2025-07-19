package ir.dekot.kavosh.feature_deviceInfo.viewModel

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.dekot.kavosh.feature_deviceInfo.model.DeviceInfo
import ir.dekot.kavosh.feature_deviceInfo.model.repository.DeviceInfoRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel مسئول مدیریت فرآیند اسکن دستگاه
 * شامل منطق اسکن، پیشرفت و وضعیت‌های مربوطه
 */
@HiltViewModel
class DeviceScanViewModel @Inject constructor(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress = _scanProgress.asStateFlow()

    private val _scanStatusText = MutableStateFlow("آماده برای اسکن...")
    val scanStatusText = _scanStatusText.asStateFlow()

    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo: StateFlow<DeviceInfo> = _deviceInfo.asStateFlow()

    /**
     * بارگذاری داده‌ها برای اجراهای غیر اول
     * در صورت نیاز، اطلاعات را مجدداً واکشی می‌کند
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun loadDataForNonFirstLaunch(activity: Activity) {
        // اگر کش داریم، این متد نباید دوباره اسکن کند
        // اگر اجرای اول برنامه باشد، این تابع کاری انجام نمی‌دهد.
        if (settingsRepository.isFirstLaunch()) return

        // **منطق جدید و کلیدی برای حل مشکل**
        // بررسی می‌کنیم که آیا اطلاعات بارگذاری شده از کش، قدیمی است یا نه.
        // نشانه ما برای قدیمی بودن کش، خالی بودن لیست برنامه‌هاست.
        if (_deviceInfo.value.apps.isEmpty()) {
            viewModelScope.launch {
                // به صورت آرام و در پس‌زمینه، اطلاعات را مجددا واکشی می‌کنیم.
                val freshInfo = fetchAllDeviceInfo(activity)
                // کش را با اطلاعات جدید و کامل به‌روزرسانی می‌کنیم.
                settingsRepository.saveDeviceInfoCache(freshInfo)
                // وضعیت UI را با اطلاعات جدید به‌روز می‌کنیم.
                _deviceInfo.value = freshInfo
            }
        }
    }

    /**
     * شروع فرآیند اسکن دستگاه
     * @param activity Activity مورد نیاز برای دسترسی به برخی اطلاعات
     * @param onScanComplete callback برای اطلاع از تکمیل اسکن
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun startScan(activity: Activity, onScanComplete: () -> Unit) {
        if (_isScanning.value) return

        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f

            val animationJob = launch {
                launch { for (i in 1..100) { delay(150); _scanProgress.value = i / 100f } }
                _scanStatusText.value = "در حال خواندن مشخصات دستگاه..."
                delay(5000)
                _scanStatusText.value = "دریافت اطلاعات از درایور ها..."
                delay(5000)
                _scanStatusText.value = "ثبت اطلاعات..."
            }

            val dataLoadingJob = launch {
                _deviceInfo.value = fetchAllDeviceInfo(activity)
                // **اصلاح کلیدی: ذخیره اطلاعات در کش پس از اسکن موفق**
                settingsRepository.saveDeviceInfoCache(_deviceInfo.value)
            }

            animationJob.join()
            dataLoadingJob.join()
            settingsRepository.setFirstLaunchCompleted()

            onScanComplete() // به لایه ناوبری اطلاع می‌دهیم که اسکن تمام شد
            _isScanning.value = false
        }
    }

    /**
     * واکشی تمام اطلاعات دستگاه
     * @param activity Activity مورد نیاز
     * @return اطلاعات کامل دستگاه
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun fetchAllDeviceInfo(activity: Activity): DeviceInfo {
        return deviceInfoRepository.getDeviceInfo(activity)
    }

    /**
     * به‌روزرسانی اطلاعات دستگاه
     * @param deviceInfo اطلاعات جدید دستگاه
     */
    fun updateDeviceInfo(deviceInfo: DeviceInfo) {
        _deviceInfo.value = deviceInfo
    }
}
