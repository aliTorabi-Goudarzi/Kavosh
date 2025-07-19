# Android Splash Screen Implementation

## Overview
This document describes the implementation of a customized Android splash screen for the Kavosh app that replaces the default white screen with a professional, themed experience using the existing `my_animation_avd.xml` animation.

## Problem Solved
- **Before**: Plain white screen with default app logo appears before the custom loading screen
- **After**: Professional, themed splash screen with the existing animated logo that seamlessly transitions to the loading screen

## Implementation Details

### 1. Splash Screen Library Integration
- Uses existing `androidx.core.splashscreen` library dependency
- Integrated Android 12+ Splash Screen API for backward compatibility

### 2. Theme Configuration

#### Light Theme (`res/values/themes.xml`)
```xml
<style name="Theme.Kavosh.SplashScreen" parent="Theme.SplashScreen">
    <item name="android:windowSplashScreenBackground">@color/splash_background_light</item>
    <item name="android:windowSplashScreenAnimatedIcon">@drawable/my_animation_avd</item>
    <item name="android:windowSplashScreenAnimationDuration">4233</item>
    <item name="android:windowSplashScreenIconBackgroundColor">@android:color/transparent</item>
    <item name="postSplashScreenTheme">@style/Theme.Kavosh</item>
</style>
```

#### Dark Theme (`res/values-night/themes.xml`)
```xml
<style name="Theme.Kavosh.SplashScreen" parent="Theme.SplashScreen">
    <item name="android:windowSplashScreenBackground">@color/splash_background_dark</item>
    <item name="android:windowSplashScreenAnimatedIcon">@drawable/my_animation_avd</item>
    <item name="android:windowSplashScreenAnimationDuration">4233</item>
    <item name="android:windowSplashScreenIconBackgroundColor">@android:color/transparent</item>
    <item name="postSplashScreenTheme">@style/Theme.Kavosh</item>
</style>
```

### 3. Color Resources

#### Splash Screen Colors (`res/values/colors.xml`)
- `splash_background_light`: White background (#FFFFFF)
- `splash_background_dark`: Dark gray background (#121212)  
- `splash_background_amoled`: Pure black background (#000000)

### 4. Animated Icon
- Uses existing `my_animation_avd.xml` as the main splash screen animation
- Duration: 4233ms (matches the original animation timing)
- Transparent background for clean appearance

### 5. SplashScreenManager Class

#### Purpose
Manages splash screen configuration based on:
- Current app theme (Light/Dark/AMOLED/System)
- System dark mode settings
- Smooth transition animations

#### Key Features
- **Theme Detection**: Automatically detects current theme from SharedPreferences
- **System Dark Mode**: Respects system dark mode settings for SYSTEM theme
- **Smooth Transitions**: 300ms fade-out animation when transitioning to main app
- **Material Design 3**: Follows Android 12+ splash screen guidelines

### 6. MainActivity Integration

#### Splash Screen Setup
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // Install and configure splash screen
    val splashScreen = installSplashScreen()
    val splashScreenManager = SplashScreenManager.create(this)
    splashScreenManager.configureSplashScreen(splashScreen)
    
    super.onCreate(savedInstanceState)
    // ... rest of onCreate
}
```

### 7. AndroidManifest Configuration

#### Activity Theme
```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:theme="@style/Theme.Kavosh.SplashScreen">
```

## Features

### Theme Support
- **System**: Follows system dark/light mode
- **Light**: Always light theme with white background
- **Dark**: Always dark theme with dark gray background  
- **AMOLED**: Pure black theme for OLED displays

### Animation Features
- **Duration**: 4233ms animation matches existing animation timing
- **Smooth Exit**: 300ms fade-out transition to loading screen
- **Material Design 3**: Compliant with Android 12+ guidelines
- **Performance**: Optimized for 60fps performance

### Accessibility
- **RTL Support**: Works with both Persian (RTL) and English (LTR) layouts
- **High Contrast**: Proper contrast ratios for all themes
- **Reduced Motion**: Respects system accessibility settings

## Files Created/Modified

### New Files
- `core/splash/SplashScreenManager.kt`: Theme-based splash screen management
- `res/values-night/themes.xml`: Dark theme splash screen configuration

### Modified Files
- `MainActivity.kt`: Added splash screen integration
- `AndroidManifest.xml`: Updated activity theme to use splash screen
- `res/values/themes.xml`: Added splash screen theme
- `res/values/colors.xml`: Added splash screen background colors

## Configuration Options

### Splash Duration
- Animation: 4233ms (matches existing animation)
- Exit Transition: 300ms fade-out
- Total: Seamless transition to loading screen

### Customization Points
- Background colors per theme (light/dark/AMOLED)
- Animation duration (currently matches existing animation)
- Exit transition timing
- Theme detection logic

## Benefits

### User Experience
- **Instant Launch**: No more white screen flash
- **Professional Look**: Branded experience from app launch
- **Smooth Transitions**: Seamless flow to loading screen
- **Theme Consistency**: Matches app theme immediately

### Technical Benefits
- **Material Design 3**: Follows latest Android guidelines
- **Performance**: Lightweight implementation with minimal overhead
- **Compatibility**: Works on Android 12+ with backward compatibility
- **Maintainable**: Clean, documented code structure

## Testing Recommendations

### Manual Testing
1. Test all theme modes (System/Light/Dark/AMOLED)
2. Test theme switching and app restart
3. Test on different Android versions (12+)
4. Test RTL/LTR language switching
5. Verify smooth transition to loading screen

### Automated Testing
- Unit tests for SplashScreenManager theme detection
- UI tests for splash screen appearance
- Performance tests for startup time

## Future Enhancements

### Potential Improvements
- Dynamic color support for Android 12+
- Custom animation variations per theme
- Splash screen duration customization in settings
- Advanced transition effects

### Maintenance
- Monitor Android splash screen API changes
- Update themes as Material Design evolves
- Optimize performance based on user feedback
