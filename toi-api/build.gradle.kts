plugins {
    application
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
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
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("io.ktor:ktor-server-cors:2.3.0")
    implementation("io.ktor:ktor-client-core:2.3.10")
    implementation("io.ktor:ktor-client-cio:2.3.10")
    implementation( "org.mindrot:jbcrypt:0.4")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("io.ktor:ktor-server-auth:2.3.4")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.4")
    implementation("com.google.api-client:google-api-client:2.2.0")
    implementation("com.google.oauth-client:google-oauth-client:1.34.1")
    implementation("com.google.http-client:google-http-client-jackson2:1.43.3")
    implementation("com.auth0:java-jwt:4.4.0")


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
