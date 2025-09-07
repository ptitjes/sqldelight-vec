plugins {
    `kotlin-dsl`
}

dependencies {
    gradleApi()

    fun DependencyHandler.plugin(dependency: Provider<PluginDependency>): Dependency =
        dependency.get().run { create("$pluginId:$pluginId.gradle.plugin:$version") }

    implementation(plugin(libs.plugins.kotlinJvm))
    implementation(plugin(libs.plugins.kotlinMultiplatform))
}
