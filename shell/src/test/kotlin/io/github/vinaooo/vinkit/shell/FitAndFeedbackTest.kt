package io.github.vinaooo.vinkit.shell

import androidx.compose.ui.unit.dp
import io.github.vinaooo.vinkit.core.AppSettings
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FitAndFeedbackTest {
    @Test
    fun `the board takes the largest box of its ratio`() {
        fit(400.dp, 600.dp, 1f) shouldBe (400.dp to 400.dp)
        fit(800.dp, 400.dp, 1f) shouldBe (400.dp to 400.dp)
        fit(300.dp, 600.dp, 0.5f) shouldBe (300.dp to 600.dp)
        fit(400.dp, 400.dp, 2f) shouldBe (400.dp to 200.dp)
    }

    @Test
    fun `feedback plays only the channels the player left on`() {
        val played = mutableListOf<String>()
        val feedback = object : GameFeedback {
            override fun sound(event: FeedbackEvent) {
                played += "sound $event"
            }

            override fun haptic(event: FeedbackEvent) {
                played += "haptic $event"
            }
        }
        feedback.give(FeedbackEvent.MOVE, AppSettings())
        feedback.give(FeedbackEvent.WIN, AppSettings(soundEnabled = false))
        feedback.give(FeedbackEvent.REJECTED, AppSettings(soundEnabled = false, hapticsEnabled = false))
        played shouldBe listOf("sound MOVE", "haptic MOVE", "haptic WIN")
    }
}
