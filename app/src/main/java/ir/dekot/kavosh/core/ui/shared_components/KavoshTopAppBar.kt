package ir.dekot.kavosh.core.ui.shared_components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ir.dekot.kavosh.R

/**
 * نوار بالایی سفارشی برای استفاده در تمام صفحات برنامه
 * این کامپوننت رنگ نوار بالایی را با رنگ پس‌زمینه صفحه هماهنگ می‌کند
 * و باعث یکپارچگی بصری در کل برنامه می‌شود
 *
 * @param title عنوان نمایش داده شده در نوار بالایی
 * @param onBackClick عملکرد دکمه بازگشت (اگر null باشد، دکمه بازگشت نمایش داده نمی‌شود)
 * @param actions اقدامات اضافی که در سمت راست نوار بالایی نمایش داده می‌شوند
 * @param modifier تنظیم‌کننده برای اعمال تغییرات بیشتر
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KavoshTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    // استفاده از رنگ پس‌زمینه برای نوار بالایی برای ایجاد یکپارچگی بصری
    TopAppBar(
        title = title,
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            // استفاده از رنگ پس‌زمینه برای نوار بالایی
            containerColor = MaterialTheme.colorScheme.background,
            // تنظیم رنگ متن و آیکون‌ها متناسب با رنگ پس‌زمینه
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = modifier
    )
}

/**
 * نسخه ساده‌تر نوار بالایی با عنوان متنی
 *
 * @param title متن عنوان نوار بالایی
 * @param onBackClick عملکرد دکمه بازگشت (اگر null باشد، دکمه بازگشت نمایش داده نمی‌شود)
 * @param actions اقدامات اضافی که در سمت راست نوار بالایی نمایش داده می‌شوند
 * @param modifier تنظیم‌کننده برای اعمال تغییرات بیشتر
 */
@Composable
fun KavoshTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    KavoshTopAppBar(
        title = { Text(text = title) },
        onBackClick = onBackClick,
        actions = actions,
        modifier = modifier
    )
}
