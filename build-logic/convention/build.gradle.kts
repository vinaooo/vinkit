plugins {
    `kotlin-dsl`
    `maven-publish`
}

// JitPack serves every vinkit artifact under com.github.vinaooo.vinkit; VERSION is the git tag it builds.
group = "com.github.vinaooo.vinkit"
version = System.getenv("VERSION") ?: "local"

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.hilt.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.ktlint.gradle.plugin)
    implementation(libs.kover.gradle.plugin)

    testImplementation(platform(libs.junit5.bom))
    testImplementation(libs.junit5.jupiter)
    testImplementation(libs.kotest.assertions.core)
    testRuntimeOnly(libs.junit5.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "vinkit.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "vinkit.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "vinkit.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "vinkit.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("hilt") {
            id = "vinkit.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("jvmLibrary") {
            id = "vinkit.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("rootCoverage") {
            id = "vinkit.root.coverage"
            implementationClass = "RootCoverageConventionPlugin"
        }
        register("publish") {
            id = "vinkit.publish"
            implementationClass = "PublishConventionPlugin"
        }
        register("quality") {
            id = "vinkit.quality"
            implementationClass = "QualityConventionPlugin"
        }
    }
}
