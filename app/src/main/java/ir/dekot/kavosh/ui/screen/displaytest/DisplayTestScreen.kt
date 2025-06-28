package ir.dekot.kavosh.ui.screen.displaytest

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ir.dekot.kavosh.R
import ir.dekot.kavosh.ui.viewmodel.DisplayTestMode
import ir.dekot.kavosh.ui.viewmodel.DisplayTestViewModel
import ir.dekot.kavosh.ui.viewmodel.GradientType

// کامپوزبل برای مخفی کردن و نمایش System Bar ها
@Composable
private fun ManageSystemUi(onDispose: () -> Unit = {}) {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            onDispose()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayTestScreen(
    viewModel: DisplayTestViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val testMode by viewModel.testMode.collectAsState()

    // مدیریت دکمه بازگشت سخت‌افزاری
    BackHandler(enabled = testMode != DisplayTestMode.NONE) {
        viewModel.stopTest()
    }

    if (testMode != DisplayTestMode.NONE) {
        ManageSystemUi { viewModel.stopTest() }
    }

    // بدنه اصلی UI که بر اساس حالت تست تغییر می‌کند
    AnimatedVisibility(
        visible = testMode == DisplayTestMode.NONE,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        DisplayTestMenu(viewModel = viewModel, onBackClick = onBackClick)
    }

    AnimatedVisibility(
        visible = testMode == DisplayTestMode.DEAD_PIXEL,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        DeadPixelTestView(viewModel = viewModel)
    }

    AnimatedVisibility(
        visible = testMode == DisplayTestMode.COLOR_BANDING,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ColorBandingTestView(viewModel = viewModel)
    }
}

// **کامپوزبل‌های جدید برای هر بخش**

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisplayTestMenu(viewModel: DisplayTestViewModel, onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.display_test_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(id = R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.display_test_description), style = MaterialTheme.typography.bodyLarge)
            Button(
                onClick = { viewModel.startTest(DisplayTestMode.DEAD_PIXEL) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.dead_pixel_test)) }
            Button(
                onClick = { viewModel.startTest(DisplayTestMode.COLOR_BANDING) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.color_banding_test)) }
        }
    }
}

@Composable
private fun DeadPixelTestView(viewModel: DisplayTestViewModel) {
    val currentColor by viewModel.currentColor.collectAsState()
    var showInstruction by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        showInstruction = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentColor)
            .clickable { viewModel.nextColor() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = showInstruction, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = stringResource(R.string.dead_pixel_instruction),
                color = if (currentColor == Color.Black) Color.White else Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
    }
}

@Composable
private fun ColorBandingTestView(viewModel: DisplayTestViewModel) {
    val gradientType by viewModel.gradientType.collectAsState()

    val brush = remember(gradientType) {
        when (gradientType) {
            GradientType.GRAYSCALE -> Brush.verticalGradient(listOf(Color.White, Color.Black))
            GradientType.RED -> Brush.verticalGradient(listOf(Color.Red.copy(alpha = 1f), Color.Red.copy(alpha = 0f)))
            GradientType.GREEN -> Brush.verticalGradient(listOf(Color.Green.copy(alpha = 1f), Color.Green.copy(alpha = 0f)))
            GradientType.BLUE -> Brush.verticalGradient(listOf(Color.Blue.copy(alpha = 1f), Color.Blue.copy(alpha = 0f)))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    ) {
        // کنترلرهای انتخاب گرادیان در پایین صفحه
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GradientChip(stringResource(R.string.grayscale_gradient), gradientType == GradientType.GRAYSCALE) { viewModel.setGradientType(GradientType.GRAYSCALE) }
            GradientChip(stringResource(R.string.red_gradient), gradientType == GradientType.RED) { viewModel.setGradientType(GradientType.RED) }
            GradientChip(stringResource(R.string.green_gradient), gradientType == GradientType.GREEN) { viewModel.setGradientType(GradientType.GREEN) }
            GradientChip(stringResource(R.string.blue_gradient), gradientType == GradientType.BLUE) { viewModel.setGradientType(
                GradientType.BLUE) }
        }
    }
}

@Composable
private fun GradientChip(text: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (selected) MaterialTheme.colorScheme.primary else Color.White
        )
    ) {
        Text(text)
    }
}