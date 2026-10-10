# Changelog

Breaking changes are marked **breaking**: a game must change its code when it bumps `vinkit.tag` past them.

## 0.9.0
- PT-BR wording follows the shared glossary (`../GLOSSARY.md`): `vinkit_new_game` "Nova partida", the new-game
  confirm text says "partida", and the bug report says "problema" (`vinkit_report_bug`, `_body`, `_subject`). No key
  changed.
- Shared strings the games used to copy: `designsystem` `vinkit_difficulty` and `vinkit_difficulty_easy`/`_medium`/
  `_hard`; `settings` `vinkit_section_game`; `shell` `vinkit_restart`; `achievements` `vinkit_new_badge`, plurals
  `vinkit_new_badges`, `vinkit_badge_played`/`_won`/`_streak`, and `vinkit_badge_no_hints`.

## 0.8.0
- `core`: `AchievementProgress` (badges earned and collected sets, by the game's own string keys) and
  `AchievementRepository`; `unlock { rules }` returns the badges just earned. The rules stay in the game.
- New `achievements` module: `DataStoreAchievementRepository` (`achievements_unlocked` and `achievements_<name>` in the
  shared DataStore, the keys BattleGrid already wrote), open `BadgesViewModel`, `BadgesScreen` and `BadgesList`
  (earned in primary, locked dimmed; one TalkBack item each).

## 0.7.0
- `shell`: `GameFrame(navigation = listOf(NavigationAction(icon, label) { … }))` adds a game's own screen buttons
  before Scores and Settings (BattleGrid's badges). `NavigationButtons` takes them too. New last parameter, with a
  default: existing callers are unchanged.

## 0.6.0
- `core`: `Ranking.LOWEST_POINTS`, the fewest points first, then the fastest (BattleGrid: shots to win); `scores`
  stores and shows it like points.
- `shell`: a game's own sounds: `AndroidGameFeedback(context, sounds = mapOf("hit" to R.raw.hit))`, played with
  `feedback.give("hit", FeedbackEvent.MOVE, settings)` (the sound by name, the kit's haptic). `GameFeedback.sound(name)`
  has a default, so existing fakes still compile.
- `scores`: a game's own tabs after the modes' (BattleGrid's achievements): `ScoresViewModel(extraGroups = …)`,
  drawn by `ScoresScreen(extra = mapOf("BADGES" to { … }))`. `extra` is the screen's new last parameter: a caller
  passing `note` as a trailing lambda must name it.

## 0.5.9
- `settings`: pt-BR names the feedback section "Sons e vibração" (what it holds) instead of "Retorno".

## 0.5.8
- `scores`: a mode without a note takes no extra room (0.5.7 shifted every list down by 8dp).

## 0.5.7
- `scores`: `ScoresScreen(note = …)` shows a line under a mode's stats (Solo: cumulative Vegas's balance).

## 0.5.6
- `shell`: a finger's press on a button with a tip now works the first time: the button is no longer rebuilt when
  its tip goes, which cancelled the press (0.5.5 only fixed quick taps). A long press shows no empty bubble.

## 0.5.5
- `shell`: a `ToolbarTip` bubble is no longer focusable, so the tap on its button closes it and works at once (it
  took two taps: the first only closed the bubble).

## 0.5.4
- `scores`: the group selector uses segmented buttons only while every name fits its segment on one line; otherwise
  scrollable tabs (Solo's "Vegas cumulative" wrapped mid-word on a phone).

## 0.5.3
- `shell`: `GameFrame(sideWidth = null)` fits landscape's side columns to the info instead of 200dp, so the board gets
  the rest (Solo's sideways board).
- `shell`: a board without an aspect ratio gets no margin either: all of its room.

## 0.5.2
- `shell`: `GameSurface(color = …)` sets the game's background (Solo: its table).
- `shell`: `LocalFrameInfo` gives the board the frame's layout (landscape, large, mirrored).

## 0.5.1
- `scores`: before any game, the screen says "Win a game to see your scores here." (or, with `ranked = false`,
  "Play a game to see your stats here.") instead of staying blank.
- `ads`: logs as `VinkitAds`, not as Sudoku Trio.

## 0.5.0
- `shell`: `GameFrame(boardAspectRatio = null)` gives the board all of its room, for a board that sizes and places
  itself (Solo).
- `shell`: `ToolbarAction`s have `visible` (a hidden one leaves the toolbar, shrinking it along its length), and
  `ToolbarAction.Button` a `tip` (`ToolbarTip(text, onShown)`): a bubble pointing at the button once.
- `scores`: `rankingFor` may return null for a mode with stats only (Solo's cumulative Vegas); `ModeSection.ranking`
  carries it.
- `scores`: `ScoresScreen(points = …)` writes a score's points (`ScorePoints(text, spoken)`, e.g. dollars). A mode
  ranked `FASTEST` now leads its rows with the time and shows no points.

## 0.4.0
- **breaking** `scores`: modes are grouped into tabs (`ScoresViewModel(groupOf = …)`), with a section per mode;
  `ScoresUiState` has `groups`, `group` and `sections` instead of one mode.

## 0.3.2
- **breaking** `vinkit.android.feature` no longer adds vinkit modules: add the ones a feature uses yourself.
