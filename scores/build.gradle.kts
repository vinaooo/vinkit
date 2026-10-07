plugins {
    alias(libs.plugins.vinkit.android.library)
    alias(libs.plugins.vinkit.android.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.vinkit.publish)
}

android {
    namespace = "io.github.vinaooo.vinkit.scores"
    resourcePrefix = "vinkit_"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    api(project(":core"))
    implementation(project(":designsystem"))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.room.testing)
}
