plugins {
    alias(libs.plugins.vinkit.android.library)
    alias(libs.plugins.vinkit.android.compose)
    alias(libs.plugins.vinkit.publish)
}

android {
    namespace = "io.github.vinaooo.vinkit.settings"
    resourcePrefix = "vinkit_"
}

dependencies {
    api(project(":core"))
    implementation(project(":designsystem"))
    api(libs.androidx.datastore.preferences)

    testImplementation(libs.kotlinx.coroutines.test)
}
