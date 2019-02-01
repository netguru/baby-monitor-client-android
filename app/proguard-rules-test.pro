# Additional proguard rules for instrumentation testing
-ignorewarnings

-keepattributes *Annotation*

-dontnote junit.framework.**
-dontnote junit.runner.**

-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**
-dontwarn org.hamcrest.**
-dontwarn com.squareup.javawriter.JavaWriter

-keep class rx.plugins.** { *; }
-keep class org.junit.** { *; }
-keep class co.netguru.android.testcommons.** { *; }
-keep class android.support.test.espresso.** { *; }
-dontwarn org.hamcrest.**

-keep class android.arch.** { *; }
-keep interface android.arch.** { *; }

-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

#Firebase Database
-keep class co.netguru.baby.monitor.client.data.communication.firebase.** { *; }
