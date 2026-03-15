plugins {
    kotlin("jvm") version "1.9.25"
    id("org.junit.platform.gradle.plugin") version "1.8.2"
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}