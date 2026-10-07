package io.github.vinaooo.vinkit.bugreport

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import java.net.URLDecoder
import org.junit.jupiter.api.Test

class BugReportTest {
    private val info = ReportInfo(
        appVersion = "1.2.0 (140)",
        android = "16 (API 36)",
        device = "Motorola moto g",
        screen = "411x891dp, 420dpi",
    )
    private val game = GameReport(
        details = listOf("Settings: KILLER HARD, RIGHT hand", "Game: seed 77, 1 moves"),
        state = "H4sIAAAAAAAA",
    )

    @Test
    fun `the report starts with the player's words, then the facts to reproduce it`() {
        val body = reportBody(info, game, "The 5 didn't go in")

        body shouldStartWith "The 5 didn't go in\n\n---\n"
        body shouldContain
            "App: 1.2.0 (140)\nAndroid: 16 (API 36)\nDevice: Motorola moto g\nScreen: 411x891dp, 420dpi\n"
        body shouldContain "Settings: KILLER HARD, RIGHT hand\nGame: seed 77, 1 moves\n"
        body.substringAfter("State:\n```\n").substringBefore("\n```") shouldBe "H4sIAAAAAAAA"
    }

    @Test
    fun `an empty description says so, and no state means no state block`() {
        val body = reportBody(info, GameReport(), "  ")

        body shouldStartWith "(no description)"
        body.contains("State:") shouldBe false
    }

    @Test
    fun `the GitHub link opens a new issue on the app's repository with the title and body filled in`() {
        val url = githubIssueUrl(ReportTarget("a@b.c", "vinaooo/sudoku-trio"), "Cage & sum", "line 1\nline 2")

        url shouldStartWith "https://github.com/vinaooo/sudoku-trio/issues/new?title="
        URLDecoder.decode(url.substringAfter("title=").substringBefore("&body="), "UTF-8") shouldBe "Cage & sum"
        URLDecoder.decode(url.substringAfter("&body="), "UTF-8") shouldBe "line 1\nline 2"
    }
}
