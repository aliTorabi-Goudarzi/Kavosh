package ir.dekot.kavosh.feature_dashboard.viewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Thermostat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.dekot.kavosh.R
import ir.dekot.kavosh.feature_dashboard.model.DashboardItem
import ir.dekot.kavosh.feature_deviceInfo.model.InfoCategory
import ir.dekot.kavosh.feature_deviceInfo.model.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // --- State های مربوط به داشبورد ---
    private val _dashboardItems = MutableStateFlow<List<DashboardItem>>(emptyList())
    val dashboardItems: StateFlow<List<DashboardItem>> = _dashboardItems.asStateFlow()

    init {
        // بارگذاری آیتم‌های داشبورد در زمان ساخته شدن ViewModel
        loadDashboardItems()
    }

    /**
     * آیتم‌های داشبورد را از ریپازیتوری بارگذاری کرده و بر اساس ترتیب و وضعیت
     * نمایش ذخیره شده، آن‌ها را مرتب می‌کند.
     */
    fun loadDashboardItems() {
        viewModelScope.launch {
            val orderedCategories = settingsRepository.getDashboardOrder()
            val hiddenCategories = settingsRepository.getHiddenCategories()
            val allPossibleItems = getFullDashboardList()

            // ابتدا آیتم‌ها را بر اساس ترتیب ذخیره شده می‌چینیم
            val loadedItems = orderedCategories.mapNotNull { category ->
                allPossibleItems.find { it.category == category }?.copy(
                    isVisible = !hiddenCategories.contains(category)
                )
            }
            // سپس آیتم‌های جدیدی که ممکن است در آپدیت‌های بعدی اضافه شده باشند را به انتها اضافه می‌کنیم
            val newItems = allPossibleItems.filter { item -> loadedItems.none { it.category == item.category } }
            _dashboardItems.value = loadedItems + newItems
        }
    }

    /**
     * لیست کامل و پیش‌فرض تمام آیتم‌های ممکن در داشبورد را برمی‌گرداند.
     */
    private fun getFullDashboardList(): List<DashboardItem> {
        return listOf(
            DashboardItem(InfoCategory.SOC, R.string.category_soc, Icons.Default.Memory),
            DashboardItem(
                InfoCategory.DEVICE,
                R.string.category_device,
                Icons.Default.PhoneAndroid
            ),
            DashboardItem(InfoCategory.SYSTEM, R.string.category_system, Icons.Default.Android),
            DashboardItem(
                InfoCategory.BATTERY,
                R.string.category_battery,
                Icons.Default.BatteryFull
            ),
            DashboardItem(InfoCategory.SENSORS, R.string.category_sensors, Icons.Default.Sensors),
            DashboardItem(
                InfoCategory.THERMAL,
                R.string.category_thermal,
                Icons.Default.Thermostat
            ),
            DashboardItem(
                InfoCategory.NETWORK,
                R.string.category_network,
                Icons.Default.NetworkWifi
            ),
            DashboardItem(InfoCategory.CAMERA, R.string.category_camera, Icons.Default.PhotoCamera),
            DashboardItem(InfoCategory.SIM, R.string.category_sim, Icons.Default.SimCard),// <-- آیتم جدید
            DashboardItem(
                InfoCategory.APPS,
                R.string.category_apps,
                Icons.Default.Apps
            ) // <-- آیتم جدید

        )
    }

    /**
     * وضعیت نمایش (visibility) یک آیتم در داشبورد را تغییر می‌دهد و تغییرات را ذخیره می‌کند.
     * @param category دسته‌بندی آیتم مورد نظر.
     * @param isVisible وضعیت جدید (نمایش داده شود یا خیر).
     */
    fun onDashboardItemVisibilityChanged(category: InfoCategory, isVisible: Boolean) {
        viewModelScope.launch {
            val currentItems = _dashboardItems.value.toMutableList()
            val itemIndex = currentItems.indexOfFirst { it.category == category }
            if (itemIndex != -1) {
                currentItems[itemIndex] = currentItems[itemIndex].copy(isVisible = isVisible)
                _dashboardItems.value = currentItems
                saveDashboardChanges() // ذخیره وضعیت جدید
            }
        }
    }

    /**
     * ترتیب جدید آیتم‌های داشبورد را پس از عملیات جابجایی (Drag & Drop) ذخیره می‌کند.
     * @param orderedCategories لیست دسته‌بندی‌ها به ترتیب جدید.
     */
    fun saveDashboardOrder(orderedCategories: List<InfoCategory>) {
        viewModelScope.launch {
            // اطمینان حاصل می‌کنیم که آیتم‌های مخفی شده، ترتیب خود را از دست نمی‌دهند
            val currentItems = _dashboardItems.value
            val hiddenCategories = currentItems.filter { !it.isVisible }.map { it.category }
            val newFullOrder = orderedCategories + hiddenCategories
            settingsRepository.saveDashboardOrder(newFullOrder)
            // بازخوانی آیتم‌ها برای اطمینان از هماهنگی وضعیت
            loadDashboardItems()
        }
    }

    /**
     * تغییرات مربوط به ترتیب و وضعیت نمایش آیتم‌ها را در SharedPreferences ذخیره می‌کند.
     */
    private fun saveDashboardChanges() {
        viewModelScope.launch {
            val currentItems = _dashboardItems.value
            val newOrder = currentItems.map { it.category }
            val newHiddenSet = currentItems.filter { !it.isVisible }.map { it.category }.toSet()
            settingsRepository.saveDashboardOrder(newOrder)
            settingsRepository.saveHiddenCategories(newHiddenSet)
        }
    }
}