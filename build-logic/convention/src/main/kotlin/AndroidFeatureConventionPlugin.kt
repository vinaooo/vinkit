import io.github.vinaooo.vinkit.buildlogic.libs
import io.github.vinaooo.vinkit.buildlogic.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("vinkit.android.library")
        pluginManager.apply("vinkit.android.compose")
        pluginManager.apply("vinkit.hilt")
        dependencies {
            "implementation"(project(":domain"))
            "implementation"(project(":core:designsystem"))
            "implementation"(project(":core:ui"))
            "implementation"(libs.library("androidx-lifecycle-runtime-compose"))
            "implementation"(libs.library("androidx-lifecycle-viewmodel-compose"))
            "implementation"(libs.library("androidx-hilt-navigation-compose"))
            "implementation"(libs.library("androidx-navigation-compose"))
            "testImplementation"(testFixtures(project(":domain")))
            "testImplementation"(libs.library("kotlinx-coroutines-test"))
            "testImplementation"(libs.library("turbine"))
        }
    }
}
