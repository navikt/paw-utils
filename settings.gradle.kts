plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
    kotlin("jvm") version "1.9.20" apply false
    id("org.jmailen.kotlinter") version "4.1.0" apply false
}

rootProject.name = "paw-utils"
include(
    "kafka",
    "kafka-streams"
)

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://packages.confluent.io/maven/")
        }
    }
}
