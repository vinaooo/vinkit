# vinkit

The shared base of vinaooo's Android games (Solo, Sudoku Trio, and the next ones): build logic, versions, and the
pieces every game has, so a new game only writes its own rules, board and screens' game-specific parts.

MIT. Kotlin, Jetpack Compose, Material 3 Expressive. No Hilt inside: every module is plain classes and composables,
and each app wires them in its own Hilt module.

## Modules

Each module is its own artifact, `com.github.vinaooo.vinkit:<module>:<tag>`, served by JitPack from a git tag. An app
takes only what it uses.

| Module | What | Status |
|---|---|---|
| `build-logic` (artifact `convention`) | convention plugins `vinkit.android.application`, `.android.library`, `.android.compose`, `.android.feature`, `.hilt`, `.jvm.library`, `.quality`, `.root.coverage`; detekt rules bundled | done |
| `catalog` | the shared `libs.versions.toml` | done |
| `core` | pure Kotlin: `GameCodec<T>`, `formatElapsed`, `AppSettings` + repository, `ScoreRecord`/`GameStats`/`Ranking` + repositories | done |
| `designsystem` | `VinkitTheme` (Material 3 Expressive, dynamic color or one of 8 palettes), `ColorChoice`, `spokenElapsed` | done |
| `ads` | `AdMobBanner` in its `BannerSlot`, UMP consent before the SDK starts (`DefaultAdConsent`) | done |
| `settings` | `DataStoreAppSettingsRepository`, `SettingsScreen(gameSections = …)`, rows (`Choice`, `IconChoice`, `ToggleRow`, `LinkRow`), `NewGameConfirmDialog(text)` | done |
| `scores` | `ScoresDatabase` (Room, `vinkit_scores.db`), `RoomScoreRepository`/`RoomStatsRepository`, open `ScoresViewModel` (`groupOf`: modes grouped into tabs, a section per mode), `ScoresScreen` (`ranked = false`: stats with draws, no scores) | done |
| `bugreport` | `BugReportDialog`: email with screenshot and files, or a prefilled GitHub issue | done |
| `shell` | `GameSurface` (screenshot + bug report + announcer), `GameFrame` (portrait/landscape/phone view, hand), `GameToolbar(actions, menuOptions)`, `WinDialog(lines)`, `ModeAndTime`, `Ticker`, `AndroidGameFeedback` | done |

## Using it in a game

Games always build against a published release from JitPack, never a local copy of vinkit. `gradle.properties` holds
the newest tag (`vinkit.tag=0.4.1`); every vinkit release is followed by bumping it in each game. `settings.gradle.kts`:

```kotlin
val vinkitTag = providers.gradleProperty("vinkit.tag").get()

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
    resolutionStrategy {
        eachPlugin {
            // JitPack serves the plugins' jar, not Gradle's plugin markers.
            if (requested.id.id.startsWith("vinkit.")) useModule("com.github.vinaooo.vinkit:convention:" + providers.gradleProperty("vinkit.tag").get())
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    versionCatalogs {
        create("libs") {
            from("com.github.vinaooo.vinkit:catalog:$vinkitTag")
        }
    }
}
```

Modules: `implementation("com.github.vinaooo.vinkit:core:${providers.gradleProperty("vinkit.tag").get()}")`, or add them to
the game's own catalog.

Release builds read their AdMob IDs and upload key from `local.properties` (`vinkit.ads.appId`,
`vinkit.ads.bannerId`, `vinkit.ads.testDeviceIds`, `vinkit.signing.*`) or CI (`VINKIT_ADS_*`, `VINKIT_SIGNING_*`).
An app can add detekt overrides in its own `config/detekt/detekt.yml`.

## Wiring (Hilt in the app)

vinkit has no DI annotations; an app builds its objects in its own module:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AdsModule {
    @Provides
    fun adsConfig() = AdsConfig(
        bannerUnitId = BuildConfig.AD_BANNER_ID,
        testDeviceIds = BuildConfig.AD_TEST_DEVICE_IDS.split(',').filter(String::isNotEmpty),
        simulateEea = BuildConfig.DEBUG,
    )

    @Provides
    @Singleton
    fun adConsent(@ApplicationContext context: Context, config: AdsConfig): AdConsent =
        DefaultAdConsent(UmpConsentClient(context), MobileAdsSdk(), config)

    @Provides
    fun adBanner(config: AdsConfig, consent: AdConsent): AdBannerProvider = AdMobBanner(config, consent)
}
```

Settings: `DataStoreAppSettingsRepository(dataStore, AppSettings(themeColor = <brand>))`; the game may keep its own
keys in the same DataStore. Scores: `ScoresDatabase.create(context)`, then `RoomScoreRepository(db)` and
`RoomStatsRepository(db)`; subclass `ScoresViewModel` with `@HiltViewModel` to pass the game's modes and rankings.
Feedback: one `AndroidGameFeedback(context)` per app (`@Singleton`).

Theme: wrap the app in `VinkitTheme(themeColor = <brand or the player's choice>)`; a game's own colors (board,
cards) go in a `CompositionLocal` built from `MaterialTheme.colorScheme` inside it.

Bug report: `BugReportDialog(ReportTarget("me+game@gmail.com", "vinaooo/game"), screenshot, onDone) { GameReport(...) }`.
The module's manifest brings the `${applicationId}.reports` file provider; an app must not declare its own.

Every resource is prefixed `vinkit_`, so an app can override a string by declaring the same name.

## Developing

```
./gradlew test detekt ktlintCheck publishToMavenLocal
```

A release is a git tag; JitPack builds it with `jitpack.yml` on first request. Tags `0.0.1`, `0.2.0`, `0.2.1` and `0.4.0`
failed on JitPack (its JDK couldn't open the wrapper jar) and stay broken there: use `0.2.2` or later.
