# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/maciek/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# base option from *App Dev Note*
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes LineNumberTable,SourceFile,Signature,*Annotation*,Exceptions,InnerClasses

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

# FragmentArgs
-keep class com.hannesdorfmann.fragmentargs.** { *; }

# retrofit
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepattributes Signature
-keepattributes Exceptions

# dagger
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}
-keep class javax.inject.** { *; }
-keep class **$$ModuleAdapter
-keep class **$$InjectAdapter
-keep class **$$StaticInjection
-keep class dagger.** { *; }
-dontwarn dagger.internal.codegen.**

# stetho
-dontwarn org.apache.http.**
-keep class com.facebook.stetho.dumpapp.** { *; }
-keep class com.facebook.stetho.server.** { *; }
-dontwarn com.facebook.stetho.dumpapp.**
-dontwarn com.facebook.stetho.server.**

# leak canary
-keep class org.eclipse.mat.** { *; }
-keep class com.squareup.leakcanary.** { *; }
-dontwarn android.app.Notification

# fabric
-dontwarn com.crashlytics.android.**

# glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keepnames enum * { *; }
-keepclassmembers enum * { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

#dagger
-dontwarn com.google.errorprone.annotations.*

# Same for MediaLibrary
-keep class org.videolan.medialibrary.** { *; }
# WebRtc
-keep class org.webrtc.** { *; }
-dontwarn org.chromium.build.**
-dontwarn org.webrtc.Logging**

#Firebase Database
-keep class co.netguru.baby.monitor.client.data.communication.firebase.** { *; }

#Navigation component classes
-keep class androidx.navigation.** { *; }

#Navigation-related resources
-keep class **.R$* {
    <fields>;
}

