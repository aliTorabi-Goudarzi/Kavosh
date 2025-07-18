package ir.dekot.kavosh.feature_dashboard.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.feature_dashboard.model.DashboardItem

@Composable
fun DashboardTile(item: DashboardItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f) // مربع شکل
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // حذف کامل افکت‌های بصری کلیک
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                // *** تغییر کلیدی: استفاده از منبع برای محتوای توضیحات (Accessibility) ***
                contentDescription = stringResource(id = item.titleResId),
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            // *** تغییر کلیدی: استفاده از منبع رشته برای نمایش متن ***
            Text(text = stringResource(id = item.titleResId), fontWeight = FontWeight.Bold)
        }
    }
}