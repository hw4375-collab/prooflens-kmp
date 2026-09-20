plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget()
    jvm()
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinxSerializationJson)
            implementation(libs.kotlinxCoroutinesCore)
            implementation(libs.ktorClientCore)
            implementation(libs.ktorClientContent)
            implementation(libs.ktorClientJson)
        }
        jvmMain.dependencies {
            implementation(libs.ktorClientCio)
        }
        androidMain.dependencies {
            implementation(libs.ktorClientOkhttp)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktorClientJs)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "dev.prooflens.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
}
