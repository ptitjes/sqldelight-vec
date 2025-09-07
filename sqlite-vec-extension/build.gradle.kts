import assets.DownloadSqliteVecReleaseJvmAssetsTask

plugins {
    id("conventions.multiplatform")
    // alias(libs.plugins.androidLibrary)
    // alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain {
            dependencies {
                api(libs.sqldelightJdbcDriver)
                api(libs.sqldelightSqliteDriver)
            }
        }
    }
}

val downloadJvmAssets = tasks.register<DownloadSqliteVecReleaseJvmAssetsTask>("downloadSqliteVecReleaseJvmAssets") {
    sqliteVecVersion = libs.versions.sqliteVec.get()

    osArchPairs = listOf(
        "linux" to "aarch64",
        "linux" to "x86_64",
        "macos" to "aarch64",
        "macos" to "x86_64",
        "windows" to "x86_64",
    )
}

tasks.named("jvmProcessResources") { dependsOn(downloadJvmAssets) }
