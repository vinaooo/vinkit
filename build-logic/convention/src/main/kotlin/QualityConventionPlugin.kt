import io.github.vinaooo.vinkit.buildlogic.excludeGeneratedAndUi
import io.github.vinaooo.vinkit.buildlogic.library
import io.github.vinaooo.vinkit.buildlogic.libs
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/** Detekt + ktlint + Kover on every module: the Clean Code gate. Detekt rules ship with vinkit. */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.gitlab.arturbosch.detekt")
        pluginManager.apply("org.jlleitschuh.gradle.ktlint")
        pluginManager.apply("org.jetbrains.kotlinx.kover")

        extensions.configure<DetektExtension> {
            buildUponDefaultConfig = true
            // vinkit's rules, then the app's own overrides in config/detekt/detekt.yml when it has them.
            val bundled = resources.text.fromString(
                checkNotNull(QualityConventionPlugin::class.java.getResource("/vinkit/detekt.yml")).readText(),
            ).asFile()
            config.setFrom(bundled, rootProject.files("config/detekt/detekt.yml").filter { it.exists() })
            parallel = true
        }
        extensions.configure<KtlintExtension> {
            android.set(true)
            filter { exclude { it.file.path.contains("/build/") } }
        }
        extensions.configure<KoverProjectExtension> { excludeGeneratedAndUi() }
        dependencies {
            "detektPlugins"(libs.library("compose-rules-detekt"))
        }
    }
}
