plugins {
    alias(libs.plugins.vinkit.android.library)
    alias(libs.plugins.vinkit.android.compose)
    alias(libs.plugins.vinkit.publish)
}

android {
    namespace = "io.github.vinaooo.vinkit.designsystem"
    resourcePrefix = "vinkit_"
}

dependencies {
    api(project(":core"))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
}
