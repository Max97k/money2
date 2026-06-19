# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to hide the original source file name.
#-renamesourcefileattribute SourceFile

# Fix missing classes from Tink / Security Crypto
-dontwarn com.google.errorprone.annotations.**

-keep class com.example.money2.data.remote.dto.** { *; }
