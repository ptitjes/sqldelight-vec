plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}

val projectVersion = libs.versions.projectVersion.get()
val sqliteVecVersion = libs.versions.sqliteVec.get()

allprojects {
    group = "io.github.ptitjes.sqldelight.vec"
    version = "$projectVersion-$sqliteVecVersion"
}
