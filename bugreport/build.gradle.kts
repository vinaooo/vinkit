plugins {
    alias(libs.plugins.vinkit.android.library)
    alias(libs.plugins.vinkit.android.compose)
    alias(libs.plugins.vinkit.publish)
}

android {
    namespace = "io.github.vinaooo.vinkit.bugreport"
    resourcePrefix = "vinkit_"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material.icons.extended)
}
