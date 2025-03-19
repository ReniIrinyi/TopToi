plugins {
    id("com.github.node-gradle.node") version "3.5.1"
}

tasks.register("runBoth") {
    dependsOn(":toi-api:runBackend", "toi-client:runFrontend")
}
