package ir.dekot.kavosh.ui.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel // <-- انوتیشن برای شناسایی ViewModel توسط Hilt
@RequiresApi(Build.VERSION_CODES.R)
class BatteryViewModel @Inject constructor (private val repository: DeviceInfoRepository) : ViewModel() {

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo = _batteryInfo.asStateFlow()

    private var batteryReceiver: BroadcastReceiver? = null

    fun registerBatteryReceiver(context: Context) {
        if (batteryReceiver != null) return // جلوگیری از ثبت مجدد

        // خواندن اطلاعات اولیه باتری با استفاده از sticky broadcast
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
}