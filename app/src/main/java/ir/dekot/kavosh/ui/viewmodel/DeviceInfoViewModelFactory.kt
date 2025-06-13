package ir.dekot.kavosh.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ir.dekot.kavosh.data.repository.DeviceInfoRepository

// Factory برای ساخت ViewModel (بدون تغییر)
class DeviceInfoViewModelFactory(private val repository: DeviceInfoRepository) :
    ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceInfoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceInfoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}