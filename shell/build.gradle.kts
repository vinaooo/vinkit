plugins {
    alias(libs.plugins.vinkit.android.library)
    alias(libs.plugins.vinkit.android.compose)
    alias(libs.plugins.vinkit.publish)
}

android {
    namespace = "io.github.vinaooo.vinkit.shell"
    resourcePrefix = "vinkit_"
}

dependencies {
    api(project(":core"))
    api(project(":bugreport"))
    implementation(project(":designsystem"))
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
}
