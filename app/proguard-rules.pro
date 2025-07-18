# قوانین ProGuard برای پروژه Kavosh
# برای جزئیات بیشتر:
#   http://developer.android.com/guide/developing/tools/proguard.html

#============= Apache POI & Dependencies Rules (Consolidated) =============

# Keep all classes AND interfaces for POI and its required schemas.
# This is crucial as R8 might strip necessary interfaces.
-keep class org.apache.poi.** { *; }
-keep interface org.apache.poi.** { *; }

-keep class com.microsoft.schemas.** { *; }
-keep interface com.microsoft.schemas.** { *; }

-keep class org.openxmlformats.** { *; }
-keep interface org.openxmlformats.** { *; }

-keep class org.apache.xmlbeans.** { *; }
-keep interface org.apache.xmlbeans.** { *; }

-keep class de.rototor.pdfbox.** { *; }
-keep interface de.rototor.pdfbox.** { *; }

#---

# Consolidate all warnings for missing desktop/server classes into one block.
# Using wildcards (**) is cleaner and more effective than listing each class.
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn java.beans.**
-dontwarn javax.imageio.**
-dontwarn javax.xml.**
-dontwarn org.w3c.dom.**
-dontwarn org.xml.sax.**
-dontwarn org.ietf.jgss.**
-dontwarn org.apache.batik.**
-dontwarn org.apache.xml.security.**
-dontwarn net.sf.saxon.**
-dontwarn org.osgi.framework.**
-dontwarn aQute.bnd.annotation.**
-dontwarn edu.umd.cs.findbugs.annotations.**
# Also, suppress warnings from the POI library itself about its own structure.
-dontwarn org.apache.poi.**

# حفظ اطلاعات خط برای debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# قوانین عمومی Android
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }

# قوانین Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# قوانین Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# قوانین Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class ir.dekot.kavosh.**$$serializer { *; }
-keepclassmembers class ir.dekot.kavosh.** {
    *** Companion;
}
-keepclasseswithmembers class ir.dekot.kavosh.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# قوانین ZXing برای QR Code
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.barcodescanner.** { *; }
-dontwarn com.google.zxing.**

# قوانین Coil برای تصاویر
-keep class coil.** { *; }
-dontwarn coil.**

# قوانین عمومی برای reflection
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# حفظ کلاس‌های مدل داده
-keep class ir.dekot.kavosh.data.** { *; }
-keep class ir.dekot.kavosh.domain.** { *; }

# قوانین برای enum ها
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# قوانین برای Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# حذف log های غیرضروری در release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}