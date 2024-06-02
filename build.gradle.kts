plugins {
    kotlin("jvm") version "1.9.24"
}

group = "com.jetbrains.study.kotlin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    // testImplementation("io.kotest:kotest-runner-junit5-jvm:5.0.0")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.0.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}