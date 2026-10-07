package io.github.vinaooo.vinkit.designsystem

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.github.vinaooo.vinkit.core.ThemeColor
import io.github.vinaooo.vinkit.core.ThemeMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class ThemeSelectionTest {

    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `system mode follows the device, explicit modes override it`() {
        isDarkTheme(ThemeMode.SYSTEM, systemInDark = true) shouldBe true
        isDarkTheme(ThemeMode.SYSTEM, systemInDark = false) shouldBe false
        isDarkTheme(ThemeMode.DARK, systemInDark = false) shouldBe true
        isDarkTheme(ThemeMode.LIGHT, systemInDark = true) shouldBe false
    }

    @Test
    fun `with dynamic color off, the chosen color's scheme is used`() {
        colorSchemeFor(context, darkTheme = false, dynamicColor = false, ThemeColor.PURPLE) shouldBe PurpleColors.light
        colorSchemeFor(context, darkTheme = true, dynamicColor = false, ThemeColor.PURPLE) shouldBe PurpleColors.dark
        // Every color has its own scheme.
        ThemeColor.entries.map { paletteScheme(it, dark = false).primary }.toSet().size shouldBe ThemeColor.entries.size
    }

    @Test
    fun `dynamic color is used on Android 12 and later`() {
        colorSchemeFor(context, darkTheme = false, dynamicColor = true, ThemeColor.BLUE) shouldNotBe BlueColors.light
        colorSchemeFor(context, darkTheme = true, dynamicColor = true, ThemeColor.BLUE) shouldNotBe BlueColors.dark
    }

    @Test
    @Config(sdk = [30])
    fun `older devices fall back to the chosen color even with dynamic color on`() {
        colorSchemeFor(context, darkTheme = false, dynamicColor = true, ThemeColor.GREEN) shouldBe GreenColors.light
        colorSchemeFor(context, darkTheme = true, dynamicColor = true, ThemeColor.GREEN) shouldBe GreenColors.dark
    }
}
