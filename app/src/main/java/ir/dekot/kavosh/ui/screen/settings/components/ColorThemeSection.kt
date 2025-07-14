package ir.dekot.kavosh.ui.screen.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.settings.ColorTheme
import ir.dekot.kavosh.data.model.settings.CustomColorTheme
import ir.dekot.kavosh.data.model.settings.PredefinedColorTheme

/**
 * بخش انتخاب تم رنگی در تنظیمات
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorThemeSection(
    currentColorTheme: ColorTheme?,
    onPredefinedThemeSelected: (PredefinedColorTheme) -> Unit,
    onCustomThemeSelected: (CustomColorTheme) -> Unit,
    onResetTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomColorPicker by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // عنوان بخش
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.color_theme_selection),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // تم‌های از پیش تعریف شده
            Text(
                text = stringResource(R.string.color_theme_selection),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(PredefinedColorTheme.entries.toTypedArray()) { theme ->
                    PredefinedColorThemeItem(
                        theme = theme,
                        isSelected = currentColorTheme?.id == theme.id,
                        onClick = { onPredefinedThemeSelected(theme) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // دکمه تم سفارشی
            OutlinedButton(
                onClick = { showCustomColorPicker = true },
                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.outlinedButtonColors(
//                    containerColor = if (currentColorTheme?.isCustom == true)
//                        MaterialTheme.colorScheme.primaryContainer else Color.Transparent
//                )
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.custom_color_picker))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // دکمه بازنشانی
            if (currentColorTheme != null) {
                TextButton(
                    onClick = onResetTheme,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.reset_to_default))
                }
            }
        }
    }
    
    // دیالوگ انتخاب رنگ سفارشی
    if (showCustomColorPicker) {
        CustomColorPickerDialog(
            initialColor = currentColorTheme?.primaryColor ?: Color.Blue,
            onColorSelected = { color ->
                onCustomThemeSelected(CustomColorTheme(color))
                showCustomColorPicker = false
            },
            onDismiss = { showCustomColorPicker = false }
        )
    }
}

/**
 * آیتم تم رنگی از پیش تعریف شده
 */
@Composable
private fun PredefinedColorThemeItem(
    theme: PredefinedColorTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(theme.primaryColor)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = stringResource(theme.nameResId),
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * دیالوگ انتخاب رنگ سفارشی با انتخابگر پیشرفته
 */
@Composable
private fun CustomColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(initialColor) }
    var selectedTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.select_primary_color))
        },
        text = {
            Column {
                // پیش‌نمایش رنگ
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = CardDefaults.cardColors(containerColor = selectedColor)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.preview),
                            color = if (isLightColor(selectedColor)) Color.Black else Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // تب‌های انتخاب نوع رنگ‌گیر
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.advanced_color_picker)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.quick_color_picker)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // محتوای تب‌ها
                when (selectedTab) {
                    0 -> {
                        // انتخابگر رنگ پیشرفته با چرخ رنگی
                        ir.dekot.kavosh.ui.composables.ColorPicker(
                            selectedColor = selectedColor,
                            onColorSelected = { selectedColor = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    1 -> {
                        // انتخابگر رنگ ساده
                        ir.dekot.kavosh.ui.composables.SimpleColorPicker(
                            selectedColor = selectedColor,
                            onColorSelected = { selectedColor = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onColorSelected(selectedColor) }
            ) {
                Text(stringResource(R.string.apply_color))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.back_button))
            }
        }
    )
}

/**
 * تابع کمکی برای تشخیص رنگ‌های روشن
 */
private fun isLightColor(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance > 0.5
}
