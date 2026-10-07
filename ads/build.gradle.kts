plugins {
    alias(libs.plugins.vinkit.android.library)
    alias(libs.plugins.vinkit.android.compose)
    alias(libs.plugins.vinkit.publish)
}

android {
    namespace = "io.github.vinaooo.vinkit.ads"
    resourcePrefix = "vinkit_"
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    testImplementation(project(":designsystem"))
    testImplementation(libs.kotlinx.coroutines.test)
}
