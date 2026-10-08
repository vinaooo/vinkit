# Changelog

Breaking changes are marked **breaking**: a game must change its code when it bumps `vinkit.tag` past them.

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
