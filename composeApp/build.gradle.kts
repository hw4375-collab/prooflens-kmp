plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget()
    jvm("desktop")
    wasmJs {
        browser()
        binaries.executable()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.ktorClientCore)
            implementation(libs.composeRuntime)
            implementation(libs.composeFoundation)
            implementation(libs.composeMaterial3)
            implementation(libs.composeUi)
            implementation(libs.composeComponentsResources)
            implementation(libs.kotlinxCoroutinesCore)
        }
        androidMain.dependencies {
            implementation(libs.activityCompose)
        }
        named("desktopMain").dependencies {
            implementation("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:0.9.4.2")
        }
    }
}

tasks.matching { it.name == "checkDebugAarMetadata" }.configureEach {
    enabled = false
}

android {
    namespace = "dev.prooflens.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "dev.prooflens.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}

compose.desktop {
    application {
        mainClass = "dev.prooflens.app.MainKt"
    }
}
