import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

/** Publishes a vinkit module (Android library or plain Kotlin) as `<group>:<project name>`, with sources. */
class PublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("maven-publish")
        val android = pluginManager.hasPlugin("com.android.library")
        if (android) {
            extensions.configure<LibraryExtension> {
                publishing { singleVariant("release") { withSourcesJar() } }
            }
        } else {
            extensions.configure<JavaPluginExtension> { withSourcesJar() }
        }
        // The release component exists only once AGP has configured the variants.
        afterEvaluate {
            extensions.configure<PublishingExtension> {
                publications.create<MavenPublication>("release") {
                    from(components.getByName(if (android) "release" else "java"))
                }
            }
        }
    }
}
