package ir.dekot.kavosh.ui.screen.sensordetail.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ir.dekot.kavosh.R

// تابع کمکی برای تبدیل درجه به جهت متنی
@Composable
private fun getBearingText(bearing: Float): String {
    return when (bearing) {
        in 337.5..360.0, in 0.0..22.5 -> stringResource(R.string.compass_n)
        in 22.5..67.5 -> stringResource(R.string.compass_ne)
        in 67.5..112.5 -> stringResource(R.string.compass_e)
        in 112.5..157.5 -> stringResource(R.string.compass_se)
        in 157.5..202.5 -> stringResource(R.string.compass_s)
        in 202.5..247.5 -> stringResource(R.string.compass_sw)
        in 247.5..292.5 -> stringResource(R.string.compass_w)
        in 292.5..337.5 -> stringResource(R.string.compass_nw)
        else -> ""
    }
}