package ir.dekot.kavosh.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.ui.composables.AnimatedBottomSheet
import ir.dekot.kavosh.ui.composables.AnimatedButton
import ir.dekot.kavosh.ui.composables.AnimatedCard
import ir.dekot.kavosh.ui.composables.AnimatedExpandableContent
import ir.dekot.kavosh.ui.composables.InfoCard
import ir.dekot.kavosh.ui.composables.KavoshTopAppBar
import ir.dekot.kavosh.ui.composables.ProfessionalLoadingIndicator
import ir.dekot.kavosh.ui.composables.SkeletonLoadingAnimation
import ir.dekot.kavosh.ui.composables.WaveLoadingIndicator

/**
 * صفحه نمایش انیمیشن‌های جدید
 * برای تست و نمایش قابلیت‌های انیمیشن
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationDemoScreen(
    onBackClick: () -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var expandedCard by remember { mutableStateOf(false) }
    var loadingProgress by remember { mutableStateOf(0.3f) }
    
    Scaffold(
        topBar = {
            // استفاده از نوار بالایی سفارشی برای یکپارچگی رنگی
            KavoshTopAppBar(
                title = "نمایش انیمیشن‌ها",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // نمایش انیمیشن بارگذاری حرفه‌ای
            item {
                InfoCard(
                    title = "انیمیشن بارگذاری حرفه‌ای"
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfessionalLoadingIndicator(
                            size = 64.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "انیمیشن نبض با افکت‌های بصری پیشرفته",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // نمایش انیمیشن موجی
            item {
                InfoCard(
                    title = "انیمیشن نوار پیشرفت موجی"
                ) {
                    Column {
                        WaveLoadingIndicator(
                            progress = loadingProgress,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AnimatedButton(
                                onClick = { loadingProgress = 0.2f }
                            ) {
                                Button(
                                    onClick = { }
                                ) {
                                    Text("20%")
                                }
                            }
                            AnimatedButton(
                                onClick = { loadingProgress = 0.5f }
                            ) {
                                Button(
                                    onClick = { }
                                ) {
                                    Text("50%")
                                }
                            }
                            AnimatedButton(
                                onClick = { loadingProgress = 0.8f }
                            ) {
                                Button(
                                    onClick = { }
                                ) {
                                    Text("80%")
                                }
                            }
                        }
                    }
                }
            }
            
            // نمایش دکمه انیمیت شده
            item {
                InfoCard(
                    title = "دکمه‌های انیمیت شده"
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AnimatedButton(
                            onClick = { /* اکشن دکمه */ }
                        ) {
                            Button(
                                onClick = { }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("دکمه انیمیت شده")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "دکمه با انیمیشن فشردن و ریپل",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // نمایش کارت انیمیت شده
            item {
                AnimatedCard(
                    onClick = { expandedCard = !expandedCard }
                ) {
                    InfoCard(
                        title = "کارت انیمیت شده"
                    ) {
                        Text(
                            text = "این کارت دارای انیمیشن تعامل است. روی آن کلیک کنید.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        AnimatedExpandableContent(
                            expanded = expandedCard
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "محتوای اضافی که با انیمیشن نمایش داده می‌شود!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // نمایش Bottom Sheet
            item {
                InfoCard(
                    title = "Bottom Sheet انیمیت شده"
                ) {
                    AnimatedButton(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("نمایش Bottom Sheet")
                        }
                    }
                }
            }
            
            // نمایش انیمیشن اسکلتون
            item {
                InfoCard(
                    title = "انیمیشن اسکلتون"
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) {
                            SkeletonLoadingAnimation(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "انیمیشن بارگذاری محتوا با افکت درخشش",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
    
    // Bottom Sheet انیمیت شده
    AnimatedBottomSheet(
        visible = showBottomSheet
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Animation,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Bottom Sheet انیمیت شده",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "این Bottom Sheet با انیمیشن‌های طبیعی و فیزیک فنری نمایش داده می‌شود.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedButton(
                    onClick = { showBottomSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("بستن")
                    }
                }
            }
        }
    }
}
