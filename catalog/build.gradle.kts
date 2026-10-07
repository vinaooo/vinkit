// The versions every vinkit game builds with, published so a game's settings.gradle.kts can import them as `libs`.
plugins {
    `version-catalog`
    `maven-publish`
}

catalog {
    versionCatalog {
        from(files("../gradle/libs.versions.toml"))
    }
}

publishing {
    publications {
        create<MavenPublication>("catalog") {
            from(components["versionCatalog"])
        }
    }
}
