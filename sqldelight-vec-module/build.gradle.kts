plugins {
    id("conventions.jvm")
    // alias(libs.plugins.vanniktech.mavenPublish)
}

dependencies {
    implementation(libs.sqldelightJdbcDriver)
    implementation(libs.sqldelightDialectApi)
    compileOnly(libs.sqldelightCompilerEnv)
    implementation(libs.sqlPsi)
    implementation(libs.kotlinPoet)
}
