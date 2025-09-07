plugins {
    id("conventions.multiplatform")
    // alias(libs.plugins.androidLibrary)
    // alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation(project(":sqlite-vec-extension"))
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlinTest)
            }
        }
    }
}
