plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover) apply false
}

// JitPack serves every artifact as com.github.vinaooo.vinkit:<module>:<tag>. The same coordinates let a game's
// includeBuild("../vinkit") swap in these projects for the published ones.
allprojects {
    group = "com.github.vinaooo.vinkit"
    version = System.getenv("VERSION") ?: "local"
}

// `./gradlew test` and `publishToMavenLocal` also cover the build logic (the included build isn't a subproject).
tasks.register("test") {
    dependsOn(gradle.includedBuild("build-logic").task(":convention:test"))
}
tasks.register("publishToMavenLocal") {
    dependsOn(subprojects.map { "${it.path}:publishToMavenLocal" })
    dependsOn(gradle.includedBuild("build-logic").task(":convention:publishToMavenLocal"))
}
