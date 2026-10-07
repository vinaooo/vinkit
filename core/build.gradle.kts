plugins {
    alias(libs.plugins.vinkit.jvm.library)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

dependencies {
    api(libs.kotlinx.serialization.json)
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("core") {
            from(components["java"])
        }
    }
}
