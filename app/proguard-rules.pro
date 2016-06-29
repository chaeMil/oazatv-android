# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/share/android-studio/data/sdk/tools/proguard/proguard-android.txt
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

-dontnote org.apache.*
-dontwarn com.google.common.**
-dontwarn com.viewpagerindicator.**
-dontwarn it.sephiroth.**
-keep class com.chaemil.hgms.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class android.support.v8.renderscript.** { *; }

-keepclassmembers class com.novoda.downloadmanager.Authority {
    static final java.lang.String AUTHORITY;
}

# fixes for Fabric to get crashes
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-printmapping build/outputs/mapping/release/mapping.txt