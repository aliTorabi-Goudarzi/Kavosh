plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kapt) // <-- به این شکل صحیح است
    alias(libs.plugins.hilt) // <-- به این شکل صحیح است
    id("org.jetbrains.kotlin.plugin.serialization") // <-- این پلاگین را اضافه کنید

}

android {
    namespace = "ir.dekot.kavosh"
    compileSdk = 36

    defaultConfig {
        applicationId = "ir.dekot.kavosh"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "6.9.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // فعال‌سازی R8 برای کاهش حجم کد و مبهم‌سازی
            isMinifyEnabled = true
            // فعال‌سازی حذف منابع استفاده نشده
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        setProperty("archivesBaseName", "kavosh-${defaultConfig.versionName}")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin { // Or tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) // Or your specific JVM version
            // You can add other Kotlin compiler options here if needed
            // e.g., freeCompilerArgs.add("-X opt-in=kotlin.RequiresOptIn")
        }
    }
    buildFeatures {
        compose = true
    }
    lint {
        checkReleaseBuilds = false
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.kotlinx.serialization.json)
//    implementation(libs.androidx.media3.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.accompanist.permissions)
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent.jvm)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.coil.compose)


    // QR Code generation (already exists but ensuring it's there)
    implementation(libs.core)
    implementation(libs.zxing.android.embedded)

    implementation(libs.androidx.core.splashscreen)


}