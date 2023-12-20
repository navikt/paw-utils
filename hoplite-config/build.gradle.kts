plugins {
    kotlin("jvm")
    `maven-publish`
    id("org.jmailen.kotlinter")
}

val koTestVersion = "5.7.2"
val hopliteVersion = "2.8.0.RC3"
dependencies {
    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-toml:$hopliteVersion")
}

group = "no.nav.paw.hoplite-config"

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
    }
    repositories {
        maven {
            val mavenRepo: String by project
            val githubPassword: String by project
            setUrl("https://maven.pkg.github.com/navikt/$mavenRepo")
            credentials {
                username = "x-access-token"
                password = githubPassword
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
