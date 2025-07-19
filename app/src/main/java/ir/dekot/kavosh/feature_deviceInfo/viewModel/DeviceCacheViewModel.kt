package ir.dekot.kavosh.feature_deviceInfo.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.dekot.kavosh.feature_deviceInfo.model.AppInfo
import ir.dekot.kavosh.feature_deviceInfo.model.DeviceInfo
import ir.dekot.kavosh.feature_deviceInfo.model.repository.ApplicationRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.ConnectivityRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// یک enum برای مدیریت بهتر وضعیت لودینگ می‌سازیم
enum class AppsLoadingState { IDLE, LOADING, LOADED }

/**
 * ViewModel مسئول مدیریت کش و ذخیره‌سازی داده‌ها
 * شامل منطق کش، بارگذاری برنامه‌ها و مدیریت داده‌های محلی
 */
@HiltViewModel
class DeviceCacheViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val applicationRepository: ApplicationRepository,
    private val connectivityRepository: ConnectivityRepository
) : ViewModel() {

    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo = _deviceInfo.asStateFlow()

    // --- State های جدید و بهینه شده برای بخش برنامه‌ها ---
    private val _userApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val userApps: StateFlow<List<AppInfo>> = _userApps.asStateFlow()

    private val _systemApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val systemApps: StateFlow<List<AppInfo>> = _systemApps.asStateFlow()

    private val _appsLoadingState = MutableStateFlow(AppsLoadingState.IDLE)
    val appsLoadingState: StateFlow<AppsLoadingState> = _appsLoadingState.asStateFlow()

    // --- State های جدید برای بخش برنامه‌ها ---
    private val _appsList = MutableStateFlow<List<AppInfo>>(emptyList())
    val appsList: StateFlow<List<AppInfo>> = _appsList.asStateFlow()

    private val _isAppsLoading = MutableStateFlow(false)
    val isAppsLoading: StateFlow<Boolean> = _isAppsLoading.asStateFlow()

    init {
        // فقط تلاش می‌کنیم کش را بارگذاری کنیم.
        if (!settingsRepository.isFirstLaunch()) {
            val cachedInfo = settingsRepository.getDeviceInfoCache()
            if (cachedInfo != null) {
                _deviceInfo.value = cachedInfo
            }
        }
    }

    /**
     * کامنت: این تابع تنها زمانی فراخوانی می‌شود که کاربر وارد صفحه "برنامه‌ها" می‌شود.
     */
    /**
     * کامنت: این تابع به طور کامل بازنویسی شده است.
     * حالا یک بار اطلاعات را واکشی، پارتیشن‌بندی و در StateFlow های مجزا ذخیره می‌کند.
     */
    /**
     * کامنت: این تابع حالا تمام کارهای سنگین خود را در یک ترد پس‌زمینه (IO) انجام می‌دهد.
     * این کار ترد اصلی UI را آزاد نگه می‌دارد و از یخ‌زدگی انیمیشن و کندی ناوبری جلوگیری می‌کند.
     */
    fun loadAppsListIfNeeded() {
        // اگر در حال لودینگ یا از قبل لود شده است، کاری نکن
        if (_appsLoadingState.value != AppsLoadingState.IDLE) return

        viewModelScope.launch(Dispatchers.IO) {
            val currentCount = applicationRepository.getCurrentPackageCount()
            val cachedCount = applicationRepository.getPackageCountCache()

            // **شرط اصلی برای استفاده از کش**
            if (currentCount == cachedCount) {
                val cachedUserApps = applicationRepository.getUserAppsCache()
                val cachedSystemApps = applicationRepository.getSystemAppsCache()

                if (cachedUserApps != null && cachedSystemApps != null) {
                    _userApps.value = cachedUserApps
                    _systemApps.value = cachedSystemApps
                    _appsLoadingState.value = AppsLoadingState.LOADED
                    return@launch // از کش استفاده شد، عملیات پایان یافت
                }
            }

            // **اگر کش معتبر نبود، واکشی کامل را شروع کن**
            _appsLoadingState.value = AppsLoadingState.LOADING // StateFlow is thread-safe // postValue برای ترد پس‌زمینه

            val allApps = applicationRepository.getAppsInfo()
            val (user, system) = allApps.partition { !it.isSystemApp }

            // ذخیره نتایج جدید در کش
            applicationRepository.saveAppsCache(user, system, allApps.size)

            _userApps.value = user
            _systemApps.value = system
            _appsLoadingState.value = AppsLoadingState.LOADED
        }
    }

    /**
     * بارگذاری لیست برنامه‌ها
     * این متد برنامه‌ها را از کش یا مستقیماً از سیستم بارگذاری می‌کند
     */
    fun loadApps() {
        if (_appsLoadingState.value == AppsLoadingState.LOADING) return

        viewModelScope.launch {
            _appsLoadingState.value = AppsLoadingState.LOADING
            _isAppsLoading.value = true

            try {
                // ابتدا سعی می‌کنیم از کش بخوانیم
                val cachedUserApps = applicationRepository.getUserAppsCache()
                val cachedSystemApps = applicationRepository.getSystemAppsCache()

                if (cachedUserApps != null && cachedSystemApps != null) {
                    _userApps.value = cachedUserApps
                    _systemApps.value = cachedSystemApps
                    _appsList.value = cachedUserApps + cachedSystemApps
                    _appsLoadingState.value = AppsLoadingState.LOADED
                    _isAppsLoading.value = false
                    return@launch
                }

                // اگر کش خالی بود، از سیستم بخوانیم
                val allApps = applicationRepository.getInstalledApps()
                val user = allApps.filter { !it.isSystemApp }
                val system = allApps.filter { it.isSystemApp }

                _appsList.value = allApps

                // ذخیره در کش
                applicationRepository.saveAppsCache(user, system, allApps.size)

                _userApps.value = user
                _systemApps.value = system
                _appsLoadingState.value = AppsLoadingState.LOADED
            } finally {
                _isAppsLoading.value = false
            }
        }
    }

    /**
     * واکشی اطلاعات سیم‌کارت
     * این تابع برای استفاده پس از اعطای مجوز طراحی شده است.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun fetchSimInfo() {
        viewModelScope.launch {
            val simCards = connectivityRepository.getSimInfo()
            _deviceInfo.value = _deviceInfo.value.copy(simCards = simCards)
        }
    }

    /**
     * ذخیره اطلاعات دستگاه در کش
     * @param deviceInfo اطلاعات دستگاه برای ذخیره
     */
    fun saveDeviceInfoToCache(deviceInfo: DeviceInfo) {
        settingsRepository.saveDeviceInfoCache(deviceInfo)
        _deviceInfo.value = deviceInfo
    }

    /**
     * پاک کردن کش اطلاعات دستگاه
     */
    fun clearDeviceInfoCache() {
        settingsRepository.clearDeviceInfoCache()
        _deviceInfo.value = DeviceInfo()
    }

    /**
     * بررسی اینکه آیا این اولین اجرای برنامه است یا نه
     * @return true اگر اولین اجرا باشد
     */
    fun isFirstLaunch(): Boolean = settingsRepository.isFirstLaunch()

    /**
     * تنظیم وضعیت اولین اجرا به عنوان تکمیل شده
     */
    fun setFirstLaunchCompleted() {
        settingsRepository.setFirstLaunchCompleted()
    }

    /**
     * به‌روزرسانی اطلاعات دستگاه
     * @param deviceInfo اطلاعات جدید دستگاه
     */
    fun updateDeviceInfo(deviceInfo: DeviceInfo) {
        _deviceInfo.value = deviceInfo
    }


}
