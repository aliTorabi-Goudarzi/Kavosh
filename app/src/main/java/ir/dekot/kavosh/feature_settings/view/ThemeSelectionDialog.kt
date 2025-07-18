package ir.dekot.kavosh.feature_settings.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.feature_customeTheme.Theme
import ir.dekot.kavosh.core.ui.shared_components.AnimationConstants

/**
 * دیالوگ انتخاب تم با طراحی حرفه‌ای و انیمیشن دایره‌ای
 * شامل پشتیبانی کامل از دوزبانه و Material Design 3
 */
@Composable
fun ThemeSelectionDialog(
    currentTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // انیمیشن ورود دیالوگ
    val dialogScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = AnimationConstants.NORMAL_ANIMATION_DURATION
        ),
        label = "dialogScale"
    )

    val dialogAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = AnimationConstants.NORMAL_ANIMATION_DURATION,
            delayMillis = 50
        ),
        label = "dialogAlpha"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .graphicsLayer {
                scaleX = dialogScale
                scaleY = dialogScale
                alpha = dialogAlpha
            },
        title = {
            Text(
                text = stringResource(R.string.theme_selection),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // لیست تم‌های موجود
                Theme.entries.forEach { theme ->
                    ThemeOptionItem(
                        theme = theme,
                        isSelected = currentTheme == theme,
                        onSelected = {
                            // اعمال تم جدید با انیمیشن دایره‌ای
                            onThemeSelected(theme)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.back_button),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * آیتم انتخاب تم در دیالوگ
 */
@Composable
private fun ThemeOptionItem(
    theme: Theme,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeTitle = when (theme) {
        Theme.SYSTEM -> stringResource(R.string.system_default)
        Theme.LIGHT -> stringResource(R.string.light)
        Theme.DARK -> stringResource(R.string.dark)
        Theme.AMOLED -> stringResource(R.string.amoled)
    }
    
    val themeDescription = when (theme) {
        Theme.SYSTEM -> stringResource(R.string.theme_system_desc)
        Theme.LIGHT -> stringResource(R.string.theme_light_desc)
        Theme.DARK -> stringResource(R.string.theme_dark_desc)
        Theme.AMOLED -> stringResource(R.string.theme_amoled_desc)
    }

    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // آیکون تم
            ThemeIcon(
                theme = theme,
                isSelected = isSelected
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // متن تم
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = themeTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = themeDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // رادیو باتن
            RadioButton(
                selected = isSelected,
                onClick = onSelected,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * آیکون نمایانگر هر تم
 */
@Composable
private fun ThemeIcon(
    theme: Theme,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val iconColor = when (theme) {
        Theme.SYSTEM -> MaterialTheme.colorScheme.primary
        Theme.LIGHT -> Color(0xFFFFC107) // زرد برای تم روشن
        Theme.DARK -> Color(0xFF424242) // خاکستری تیره برای تم تاریک
        Theme.AMOLED -> Color.Black // مشکی برای AMOLED
    }
    
    val backgroundColor = when (theme) {
        Theme.SYSTEM -> MaterialTheme.colorScheme.primaryContainer
        Theme.LIGHT -> Color(0xFFFFF8E1)
        Theme.DARK -> Color(0xFF303030)
        Theme.AMOLED -> Color(0xFF1A1A1A)
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(iconColor)
            )
        }
    }
}
