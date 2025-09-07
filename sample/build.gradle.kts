plugins {
    id("conventions.multiplatform")
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain {
            dependencies {
                implementation(libs.sqldelightSqliteDriver)
                implementation(project(":sqldelight-vec-driver"))
            }
        }
    }
}

sqldelight {
    databases {
        create("TestDb") {
            packageName = "io.github.ptitjes.sqldelight.vec.sample"
            module(project(":sqldelight-vec-module"))
        }
    }
}
