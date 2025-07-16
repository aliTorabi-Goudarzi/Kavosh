package ir.dekot.kavosh.ui.screen.settings.components

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
import androidx.compose.material.icons.filled.Language
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
import ir.dekot.kavosh.ui.composables.AnimationConstants

/**
 * دیالوگ انتخاب زبان با طراحی حرفه‌ای و انیمیشن‌های نرم
 * شامل پشتیبانی کامل از دوزبانه و Material Design 3
 */
@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
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

    // لیست زبان‌های پشتیبانی شده
    val supportedLanguages = listOf(
        "fa" to stringResource(R.string.persian),
        "en" to stringResource(R.string.english)
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
                text = stringResource(R.string.language_selection),
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
                // لیست زبان‌های موجود
                supportedLanguages.forEach { (languageCode, languageName) ->
                    LanguageOptionItem(
                        languageCode = languageCode,
                        languageName = languageName,
                        isSelected = currentLanguage == languageCode,
                        onSelected = {
                            // اعمال زبان جدید با انیمیشن دایره‌ای
                            // این تغییر باعث بازسازی Activity می‌شود
                            onLanguageSelected(languageCode)
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
 * آیتم انتخاب زبان در دیالوگ
 */
@Composable
private fun LanguageOptionItem(
    languageCode: String,
    languageName: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val languageDescription = when (languageCode) {
        "fa" -> stringResource(R.string.language_persian_desc)
        "en" -> stringResource(R.string.language_english_desc)
        else -> ""
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
            // آیکون زبان
            LanguageIcon(
                languageCode = languageCode,
                isSelected = isSelected
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // متن زبان
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = languageName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = languageDescription,
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
 * آیکون نمایانگر هر زبان
 */
@Composable
private fun LanguageIcon(
    languageCode: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val iconColor = when (languageCode) {
        "fa" -> Color(0xFF00A86B) // سبز برای فارسی
        "en" -> Color(0xFF1976D2) // آبی برای انگلیسی
        else -> MaterialTheme.colorScheme.primary
    }
    
    val backgroundColor = when (languageCode) {
        "fa" -> Color(0xFFE8F5E8) // پس‌زمینه سبز روشن برای فارسی
        "en" -> Color(0xFFE3F2FD) // پس‌زمینه آبی روشن برای انگلیسی
        else -> MaterialTheme.colorScheme.primaryContainer
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
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
