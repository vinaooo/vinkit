# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

vinkit is the shared kit of vinaooo's Android games (Solo, Sudoku Trio, OX Play). `README.md` lists the modules and
how a game uses them; `CHANGELOG.md` lists every release (mark **breaking** changes). Games build only against a
published tag from JitPack, never a local copy, and the user bumps `vinkit.tag` in the games by hand.

The notes below are the pitfalls behind choices already made in the kit. Keep them true when changing that code.

## Commands

```bash
./gradlew test detekt ktlintCheck publishToMavenLocal
```

A release is a git tag; then fetch `https://jitpack.io/com/github/vinaooo/vinkit/<tag>/build.log` to start the
JitPack build.

## build-logic (`convention`)

- **Kover:** the root report does **not** inherit the modules' filters, so `KoverFilters.kt` is one filter function
  shared by the module plugin and `vinkit.root.coverage` (composables, Hilt, Room `_Impl`, serializers, `di`
  packages, `*Activity`, `*Application`).
- **JUnit Platform:** JUnit 5 plus the vintage engine for Robolectric's JUnit 4 tests, `failOnNoDiscoveredTests =
  false`, and `--add-opens java.base/jdk.internal.access` for Robolectric at SDK 36+ (`KotlinAndroid.kt`).
- **Signing (`ReleaseSigning`):** reads `local.properties` or `VINKIT_SIGNING_*` env vars (env wins). With nothing
  set, the build is unsigned; a partial setup fails and names what's missing.
- **Versions (`AppVersion`):** `versionCode` = `git rev-list --count HEAD`, `versionName` = the latest `vX.Y.Z` tag
  (`0.0.0-g<hash>` before the first one). A shallow clone fails the build; outside git it falls back to a placeholder.
- **Ad IDs (`AdIds`):** debug builds always get Google's test IDs. Release builds read `local.properties` or CI env,
  fall back to test IDs, and reject partial or swapped IDs: an App ID has `~`, an ad unit has `/`. The App ID goes
  into a manifest placeholder, the banner ID into BuildConfig.
- Each resolver has its JUnit 5 tests in `build-logic`, wired into the root `test` task.

## ads

- **Size:** `AdSize.getInlineAdaptiveBannerAdSize(width, maxHeight)`, capped at 60dp. Full width on a phone in
  portrait; in landscape or on a tablet (600dp+), at most 320dp wide and centered, and 50dp tall in landscape. The
  large anchored size is about 130dp on a phone, and the other anchored sizes are deprecated.
- `BannerSlot` always reserves the slot's full-width height, so the board never jumps when an ad loads or fails.
  Create a new `AdView` when the width changes, and tie `pause`/`resume`/`destroy` to the lifecycle.
- **Consent first:** on every launch, update the consent status with UMP and show the form if required; only then
  start the Mobile Ads SDK (off the main thread, once). Previous-session consent starts ads at once. UMP and Mobile
  Ads sit behind `ConsentClient` and `AdsSdk`, so that order is unit-tested with fakes.
- Debug builds use `DebugGeography.EEA`, which only works on test devices (emulators, or hashed IDs from logcat).

## shell

- **Landscape:** both sides of the board get the width of the wider one (`CenteredRow`), so the board stays
  centered; the row pads for `safeDrawing` (cutouts, side navigation bars).
- **Toolbar:** a contextual button grows into the toolbar only along it: growing across it leaves the toolbar's
  padding stale. The toolbar keeps its height when that button leaves.
- **New-game menu:** Material's `FloatingActionButtonMenu` clips its items, so the pills are drawn in our own popup
  with 40dp of room around them. While it's open, a 60% scrim dims the game and the toolbar shrinks to its close
  button.
- **Hand and board position:** mirror the board and its controls; a bottom board sits as low as it can while leaving
  room for its largest possible state, so it never moves during play.
- **Phone view** (tablets only): board, its controls and the toolbar as one column at most 412dp wide, on the side
  Settings picks, also in landscape (the board shrinks to fit). Stats and navigation stay in the top bar.
- **Bug report:** capture the board from a graphics layer once the menu and its scrim have closed. Never embed a
  token in the app: it can be extracted.
- **Announcer:** a tiny live region, because `View.announceForAccessibility` is deprecated at API 36. Never make it
  a direct child of a `Surface`: `propagateMinConstraints` stretches it over the whole screen and blocks touch
  exploration. It's wrapped in a `Box`, with a test on its size.

## settings and scores

- 4+ options (`IconChoice`): a connected icon `ToggleButton` group (radio semantics, each icon named for TalkBack)
  with the chosen option's name and description below it, bouncing in.
- **Reveal:** an option that depends on a switch expands from under it (`expandVertically` from the top + `fadeIn`,
  reversed on hide).
- **Color row:** eight `ThemeColor`s as 32dp circles in 48dp targets, revealed when dynamic color is off and always
  shown below Android 12. The schemes were generated once with material-color-utilities (`SchemeTonalSpot`) and
  committed as constants: no runtime color library.
- Each screen sets its own content color (`Surface(color, contentColor)`) instead of relying on the host.
