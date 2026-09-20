plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

application {
    mainClass.set("dev.prooflens.server.ApplicationKt")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.ktorServerCore)
    implementation(libs.ktorServerNetty)
    implementation(libs.ktorServerContent)
    implementation(libs.ktorServerCors)
    implementation(libs.ktorServerCallLogging)
    implementation(libs.ktorServerConfig)
    implementation(libs.kotlinxSerializationJson)
    implementation(libs.ktorClientCore)
    implementation(libs.ktorClientContent)
    implementation(libs.ktorClientJson)
    implementation(libs.ktorClientCio)
}
