-dontwarn org.slf4j.impl.StaticLoggerBinder

# WorkManager instantiates InputMerger subclasses via reflection (Class.newInstance()).
# Without this rule, R8 strips the no-arg constructor and Glance's SessionWorker fails
# to start, leaving the widget stuck on its placeholder layout.
-keep class * extends androidx.work.InputMerger {
    <init>();
}

# Glance instantiates ActionCallback implementations reflectively from the class name
# stored in actionRunCallback<T>(...). Without this, R8 renames CopyPromptAction in
# release and the widget's copy button silently does nothing.
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    <init>();
}

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile