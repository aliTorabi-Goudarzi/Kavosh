package ir.dekot.kavosh.ui.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// این متغیرها را بالای کلاس ViewModel اضافه کنید
private var socPollingJob: Job? = null


@RequiresApi(Build.VERSION_CODES.R)
class DeviceInfoViewModel(private val repository: DeviceInfoRepository) : ViewModel() {

    // --- State های اصلی ---

    // State جدید برای لیست ترکیبی اطلاعات دما
// State برای نگهداری فرکانس‌های زنده CPU
    private val _liveCpuFrequencies = MutableStateFlow<List<String>>(emptyList())
    val liveCpuFrequencies = _liveCpuFrequencies.asStateFlow()

    // State برای نگهداری لود زنده GPU
    private val _liveGpuLoad = MutableStateFlow<Int?>(null)
    val liveGpuLoad = _liveGpuLoad.asStateFlow()

    private val _thermalDetails = MutableStateFlow<List<ThermalInfo>>(emptyList())
    val thermalDetails = _thermalDetails.asStateFlow()

    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo = _deviceInfo.asStateFlow()

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo = _batteryInfo.asStateFlow()

    // --- State های مربوط به UI و ناوبری ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen = _currentScreen.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress = _scanProgress.asStateFlow()

    // State جدید برای متن در حال اسکن
    private val _scanStatusText = MutableStateFlow("آماده برای اسکن...")
    val scanStatusText = _scanStatusText.asStateFlow()

    private var batteryReceiver: BroadcastReceiver? = null

    // --- توابع ---
    init {
        if (repository.isFirstLaunch()) {
            // اگر اولین اجرا بود، صفحه اسپلش را نشان بده
            _currentScreen.value = Screen.Splash
        } else {
            // در غیر این صورت، مستقیم به داشبورد برو و داده‌ها را بدون انیمیشن بارگذاری کن
            _currentScreen.value = Screen.Dashboard
            loadDataWithoutAnimation()
        }
    }

    // این دو متد را به DeviceInfoViewModel اضافه کنید

    fun startSocPolling() {
        stopSocPolling() // جلوگیری از اجرای همزمان چند جاب
        socPollingJob = viewModelScope.launch {
            while (true) {
                _liveCpuFrequencies.value = repository.getLiveCpuFrequencies()
                _liveGpuLoad.value = repository.getGpuLoadPercentage()
                delay(1500) // هر ۱.۵ ثانیه آپدیت کن
            }
        }
    }

    fun stopSocPolling() {
        socPollingJob?.cancel()
        socPollingJob = null
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun startScan() {
        if (_isScanning.value) return // جلوگیری از اسکن مجدد

        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f

            // Job برای انیمیشن نمایشی (متن و نوار پیشرفت)
            val animationJob = launch {
                // انیمیشن نوار پیشرفت (15 ثانیه)
                launch {
                    for (i in 1..100) {
                        delay(150) // 100 * 150ms = 15s
                        _scanProgress.value = i / 100f
                    }
                }

                // انیمیشن تغییر متن
                _scanStatusText.value = "در حال خواندن مشخصات دستگاه..."
                delay(5000)
                _scanStatusText.value = "دریافت اطلاعات از درایور ها..."
                delay(5000)
                _scanStatusText.value = "ثبت اطلاعات..."
            }

            // بارگذاری اطلاعات واقعی به صورت موازی
            val dataLoadingJob = launch {
                _deviceInfo.value = DeviceInfo(
                    cpu = repository.getCpuInfo(),
                    gpu = repository.getGpuInfo(),
                    ram = repository.getRamInfo(),
                    storage = repository.getStorageInfo(),
                    display = repository.getDisplayInfo(),
                    system = repository.getSystemInfo(),
                    sensors = repository.getSensorInfo(),
                    thermal = repository.getThermalInfo() // <-- این خط را اضافه کنید
                )
            }


            // منتظر بمان تا هم انیمیشن و هم بارگذاری داده تمام شود
            animationJob.join()
            dataLoadingJob.join()

            // *** این خط را اضافه کنید ***
            repository.setFirstLaunchCompleted()

            // رفتن به داشبورد
            _currentScreen.value = Screen.Dashboard
            _isScanning.value = false
        }
    }

    // به ورودی این تابع context را اضافه کنید
    fun navigateToDetail(category: InfoCategory, context: Context) {
        if (category == InfoCategory.THERMAL) {
            prepareThermalDetails(context) // context را پاس می‌دهیم
        }
        _currentScreen.value = Screen.Detail(category)
    }


    fun navigateBack() {
        _currentScreen.value = Screen.Dashboard
    }

    // توابع اصلاح شده و امن برای باتری
    fun registerBatteryReceiver(context: Context) {
        if (batteryReceiver != null) return // جلوگیری از ثبت مجدد

        // خواندن اطلاعات اولیه باتری با استفاده از sticky broadcast (روش امن)
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent: Intent? = context.registerReceiver(null, filter)
        if (initialIntent != null) {
            _batteryInfo.value = repository.getBatteryInfo(initialIntent)
        }

        // ثبت گیرنده برای دریافت آپدیت‌های زنده
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    if (it.action == Intent.ACTION_BATTERY_CHANGED) {
                        _batteryInfo.value = repository.getBatteryInfo(it)
                    }
                }
            }
        }
        context.registerReceiver(batteryReceiver, filter)
    }

    fun unregisterBatteryReceiver(context: Context) {
        batteryReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (_: IllegalArgumentException) {
                // اگر گیرنده قبلا ثبت نشده باشد، خطا را نادیده می‌گیریم
            } finally {
                batteryReceiver = null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun loadDataWithoutAnimation() {
        viewModelScope.launch {
            _deviceInfo.value = DeviceInfo(
                cpu = repository.getCpuInfo(),
                gpu = repository.getGpuInfo(),
                ram = repository.getRamInfo(),
                storage = repository.getStorageInfo(),
                display = repository.getDisplayInfo(),
                system = repository.getSystemInfo(),
                sensors = repository.getSensorInfo(),
                thermal = repository.getThermalInfo() // <-- این خط را اضافه کنید
            )
        }
    }

    // این متد را داخل کلاس DeviceInfoViewModel اضافه کنید
    private fun prepareThermalDetails(context: Context) {
        val combinedList = mutableListOf<ThermalInfo>()

        // ۱. خواندن اطلاعات آنی باتری با استفاده از متد جدید (روش امن و صحیح)
        repository.getInitialBatteryInfo(context)?.let { batteryData ->
            if (batteryData.temperature.isNotBlank()) {
                combinedList.add(
                    ThermalInfo(
                        type = "باتری (Battery)",
                        temperature = batteryData.temperature
                    )
                )
            }
        }

        // ۲. اضافه کردن بقیه سنسورهای دمایی که از سیستم خوانده شده‌اند
        combinedList.addAll(deviceInfo.value.thermal)

        _thermalDetails.value = combinedList
    }

}