plugins {
    application
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.0"
}

application {
    mainClass.set("MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-netty:2.3.0")
    implementation("ch.qos.logback:logback-classic:1.2.11")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.register<JavaExec>("runBackend") {
    group = "application"
    description = "Starts the Ktor backend"
    mainClass.set("MainKt")
    classpath = sourceSets["main"].runtimeClasspath
}
