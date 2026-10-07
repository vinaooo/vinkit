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
| `core` | pure Kotlin: `GameCodec<T>` (bug-report state text), `formatElapsed` | done |
| `designsystem` | theme, 8 palettes, `ThemeColor`, color picker | planned |
| `ads` | AdMob banner, `BannerSlot`, UMP consent | planned |
| `settings` | common settings + DataStore + `SettingsScreen` with a game section slot | planned |
| `scores` | Room scores DB, per-mode stats, `ScoresScreen` | planned |
| `bugreport` | bug report dialog, screenshot, email / GitHub issue | planned |
| `shell` | game screen frame: floating toolbar, menus, win celebration, announcer, feedback, layouts | planned |

## Using it in a game

`gradle.properties`: `vinkit.tag=0.1.0` (a release tag). `settings.gradle.kts`:

```kotlin
val vinkitTag = providers.gradleProperty("vinkit.tag").get()
// -Pvinkit.local=true (or vinkit.local=true in ~/.gradle/gradle.properties) builds against ../vinkit's source.
val vinkitLocal = providers.gradleProperty("vinkit.local").orNull == "true"

pluginManagement {
    if (providers.gradleProperty("vinkit.local").orNull == "true") includeBuild("../vinkit/build-logic")
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
            if (vinkitLocal) from(files("../vinkit/gradle/libs.versions.toml"))
            else from("com.github.vinaooo.vinkit:catalog:$vinkitTag")
        }
    }
}

if (vinkitLocal) includeBuild("../vinkit")
```

Modules: `implementation("com.github.vinaooo.vinkit:core:${providers.gradleProperty("vinkit.tag").get()}")`, or add them to
the game's own catalog. With `vinkit.local=true`, Gradle swaps in the local
projects whatever the version, so kit and game are edited and built together without publishing.

Release builds read their AdMob IDs and upload key from `local.properties` (`vinkit.ads.appId`,
`vinkit.ads.bannerId`, `vinkit.ads.testDeviceIds`, `vinkit.signing.*`) or CI (`VINKIT_ADS_*`, `VINKIT_SIGNING_*`).
An app can add detekt overrides in its own `config/detekt/detekt.yml`.

## Developing

```
./gradlew test detekt ktlintCheck publishToMavenLocal
```

A release is a git tag; JitPack builds it with `jitpack.yml` on first request.
