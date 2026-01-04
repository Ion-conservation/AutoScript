import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.compose.compiler)
}


android {
    namespace = "auto.script"
    compileSdk {
        version = release(36)
    }

    signingConfigs {
        getByName("debug")
        // release 暂时使用 debug 签名，正式发布时需要配置真实签名
    }

    defaultConfig {
        applicationId = "auto.script"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "APP_PACKAGE_NAME", "\"com.netease.cloudmusic\"")

    }


    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            // Preview 用，不需要强制 Shizuku 重新授权
            buildConfigField("boolean", "FORCE_SHIZUKU_REAUTH", "false")
            resValue("string", "app_name", "调试版")
            resValue("string", "appA11yServiceName", "A 调试版")
        }

        getByName("release") {
            applicationIdSuffix = ".release"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 暂时使用 debug 签名，正式发布时需要配置真实签名
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("boolean", "FORCE_SHIZUKU_REAUTH", "false")
            resValue("string", "app_name", "正式版")
            resValue("string", "appA11yServiceName", "A 正式版")
        }

        create("dev") {
            // 关键：基于 debug 复制一份，再覆盖差异配置
            initWith(getByName("debug"))

            // 包名后缀，区分 auto.script 与 auto.script.dev
            applicationIdSuffix = ".dev"

            // 版本名后缀，方便区分
            versionNameSuffix = "-dev"

            // 使用 debug 签名，跟 release 分开
            signingConfig = signingConfigs.getByName("debug")

            // dev 强制重新走 Shizuku 授权流程
            buildConfigField("boolean", "FORCE_SHIZUKU_REAUTH", "true")
            resValue("string", "app_name", "开发版")
            resValue("string", "appA11yServiceName", "A 开发版")
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21

    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.addAll("-Xjvm-default=all", "-opt-in=kotlin.RequiresOptIn")
        }
    }
    buildFeatures {
        compose = true
        aidl = true
        buildConfig = true
    }


}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.core)
    implementation(libs.androidx.compose.material.extended)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // 1. 核心测试运行器，用于启动 Instrumentation
    // 使用 bundle 引用可以减少行数


    // UI Automator 脚本 (NetEaseMusicAutomationScript.kt) 所需的依赖
    // 必须在 androidTestImplementation scope 下
    androidTestImplementation(libs.androidx.uiautomator)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.shizuku.hidden)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room 数据库
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)
}

kotlin {
    jvmToolchain(21)
}
