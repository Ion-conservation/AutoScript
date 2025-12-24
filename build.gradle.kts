// Top-level build file where you can add configuration options common to all sub-projects/modules.


plugins {
    // Android Application 插件（供子模块使用）
    alias(libs.plugins.android.application) apply false

    // Kotlin Android 插件（供子模块使用）
    alias(libs.plugins.kotlin.android) apply false

    // Kotlin Kapt 插件（供子模块使用）
    alias(libs.plugins.kotlin.kapt) apply false

    // Hilt 插件（供子模块使用）
    alias(libs.plugins.hilt.android) apply false

    alias(libs.plugins.compose.compiler) apply false
}
