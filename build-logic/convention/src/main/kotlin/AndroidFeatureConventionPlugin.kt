import io.github.vinaooo.vinkit.buildlogic.libs
import io.github.vinaooo.vinkit.buildlogic.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * A feature module: an Android library with Compose and Hilt, ViewModels and navigation. It depends on the game's
 * `:domain` (and its test fakes) when the game has one; vinkit modules are added by the feature itself.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("vinkit.android.library")
        pluginManager.apply("vinkit.android.compose")
        pluginManager.apply("vinkit.hilt")
        dependencies {
            rootProject.findProject(":domain")?.let { domain ->
                "implementation"(domain)
                "testImplementation"(testFixtures(domain))
            }
            "implementation"(libs.library("androidx-lifecycle-runtime-compose"))
            "implementation"(libs.library("androidx-lifecycle-viewmodel-compose"))
            "implementation"(libs.library("androidx-hilt-navigation-compose"))
            "implementation"(libs.library("androidx-navigation-compose"))
            "testImplementation"(libs.library("kotlinx-coroutines-test"))
            "testImplementation"(libs.library("turbine"))
        }
    }
}
