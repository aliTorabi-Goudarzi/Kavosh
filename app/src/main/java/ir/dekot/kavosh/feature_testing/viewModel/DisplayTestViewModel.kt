package ir.dekot.kavosh.feature_testing.viewModel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class DisplayTestMode { NONE, DEAD_PIXEL, COLOR_BANDING }
enum class GradientType { GRAYSCALE, RED, GREEN, BLUE }

@HiltViewModel
class DisplayTestViewModel @Inject constructor() : ViewModel() {

    private val _testMode = MutableStateFlow(DisplayTestMode.NONE)
    val testMode: StateFlow<DisplayTestMode> = _testMode.asStateFlow()

    // --- منطق تست پیکسل سوخته ---
    private val deadPixelTestColors = listOf(Color.Black, Color.White, Color.Red, Color.Green, Color.Blue)
    private val _currentColorIndex = MutableStateFlow(0)

    // **اصلاح ۱: تعریف صحیح StateFlow برای رنگ فعلی**
    private val _currentColor = MutableStateFlow(deadPixelTestColors.first())
    val currentColor: StateFlow<Color> = _currentColor.asStateFlow()

    // --- منطق تست گرادیان رنگ ---
    private val _gradientType = MutableStateFlow(GradientType.GRAYSCALE)
    val gradientType: StateFlow<GradientType> = _gradientType.asStateFlow()

    fun startTest(mode: DisplayTestMode) {
        // ریست کردن وضعیت‌ها قبل از شروع تست جدید
        _currentColorIndex.value = 0
        _currentColor.value = deadPixelTestColors.first()
        _gradientType.value = GradientType.GRAYSCALE
        _testMode.value = mode
    }

    fun stopTest() {
        _testMode.value = DisplayTestMode.NONE
    }

    fun nextColor() {
        if (_testMode.value == DisplayTestMode.DEAD_PIXEL) {
            val nextIndex = (_currentColorIndex.value + 1) % deadPixelTestColors.size
            _currentColorIndex.value = nextIndex
            // **اصلاح ۲: آپدیت مستقیم مقدار StateFlow**
            _currentColor.value = deadPixelTestColors[nextIndex]
        }
    }

    fun setGradientType(type: GradientType) {
        _gradientType.value = type
    }
}