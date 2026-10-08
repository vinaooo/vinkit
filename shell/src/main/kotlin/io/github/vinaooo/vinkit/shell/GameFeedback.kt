package io.github.vinaooo.vinkit.shell

import io.github.vinaooo.vinkit.core.AppSettings

enum class FeedbackEvent { MOVE, REJECTED, WIN }

/**
 * Sound and vibration. The game decides when; implementations decide how. [FeedbackEvent.REJECTED] is for moves the
 * rules refuse.
 */
interface GameFeedback {
    fun sound(event: FeedbackEvent)

    fun haptic(event: FeedbackEvent)

    /** A game's own sound ("hit", "sunk"), by the name it was given in `AndroidGameFeedback(sounds = …)`. */
    fun sound(name: String) = Unit
}

/** Plays [event] through the channels the player left on in [settings]. */
fun GameFeedback.give(event: FeedbackEvent, settings: AppSettings) {
    if (settings.soundEnabled) sound(event)
    if (settings.hapticsEnabled) haptic(event)
}

/** Plays the game's own sound [name] with the kit's [haptic], through the channels left on in [settings]. */
fun GameFeedback.give(name: String, haptic: FeedbackEvent, settings: AppSettings) {
    if (settings.soundEnabled) sound(name)
    if (settings.hapticsEnabled) haptic(haptic)
}
