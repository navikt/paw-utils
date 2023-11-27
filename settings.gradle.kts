plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
    kotlin("jvm") version "1.9.10" apply false
    id("org.jmailen.kotlinter") version "4.0.0" apply false
}

rootProject.name = "paw-utils"
include(
    "kafka"
)

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://packages.confluent.io/maven/")
        }
    }
}
