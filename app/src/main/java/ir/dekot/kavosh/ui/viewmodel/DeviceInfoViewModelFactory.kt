package ir.dekot.kavosh.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ir.dekot.kavosh.data.repository.DeviceInfoRepository

// Factory حالا می‌تواند هر سه نوع ViewModel را بسازد
@Suppress("UNCHECKED_CAST")
class DeviceInfoViewModelFactory(private val repository: DeviceInfoRepository) :
    ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DeviceInfoViewModel::class.java) -> {
                DeviceInfoViewModel(repository) as T
            }
            modelClass.isAssignableFrom(BatteryViewModel::class.java) -> {
                BatteryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SocViewModel::class.java) -> {
                SocViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}