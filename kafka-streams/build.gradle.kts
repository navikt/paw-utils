plugins {
    kotlin("jvm")
    `maven-publish`
    id("org.jmailen.kotlinter")
}

val koTestVersion = "5.7.2"

dependencies {
    api(project(":kafka"))
    implementation("org.apache.kafka:kafka-clients:3.6.0")
    implementation("org.apache.kafka:kafka-streams:3.6.0")
    implementation("io.confluent:kafka-streams-avro-serde:7.4.0")

    // Test
    testImplementation("io.kotest:kotest-runner-junit5:$koTestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$koTestVersion")
}

group = "no.nav.paw.kafka-streams"

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
