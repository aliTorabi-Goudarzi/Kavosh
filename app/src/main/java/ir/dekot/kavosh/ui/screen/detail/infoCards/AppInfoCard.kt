// Path: app/src/main/java/ir/dekot/kavosh/ui/screen/detail/infoCards/AppInfoCard.kt
package ir.dekot.kavosh.ui.screen.detail.infoCards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.components.AppInfo
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppInfoCard(info: AppInfo) {
    var isExpanded by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val context = LocalContext.current
    val pm = context.packageManager

    // آیکون به صورت پویا در لحظه بارگذاری می‌شود
    val icon = try {
        pm.getApplicationIcon(info.packageName)
    } catch (_: Exception) {
        null // در صورت خطا، آیکون خالی می‌ماند
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(model = icon), // استفاده از آیکون بارگذاری شده,
                    contentDescription = info.appName,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(info.appName, style = MaterialTheme.typography.titleMedium)
                    Text(info.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        text = stringResource(R.string.app_version, info.versionName, info.versionCode),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.app_installed_on, dateFormatter.format(Date(info.installTime))),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.app_permissions),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (info.permissions.isEmpty()) {
                        Text(
                            text = stringResource(R.string.app_no_permissions),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        info.permissions.sorted().forEach { permission ->
                            Text(
                                text = "• ${permission.substringAfterLast('.')}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}