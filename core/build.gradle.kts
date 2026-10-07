plugins {
    alias(libs.plugins.vinkit.jvm.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.vinkit.publish)
}

dependencies {
    api(libs.kotlinx.serialization.json)
}
