import com.android.build.api.dsl.LibraryExtension
import io.github.vinaooo.vinkit.buildlogic.configureJUnitPlatform
import io.github.vinaooo.vinkit.buildlogic.configureKotlinAndroid
import io.github.vinaooo.vinkit.buildlogic.libs
import io.github.vinaooo.vinkit.buildlogic.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("vinkit.quality")
        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
            testOptions.targetSdk = libs.version("targetSdk").toInt()
            lint.targetSdk = libs.version("targetSdk").toInt()
        }
        configureJUnitPlatform()
    }
}
