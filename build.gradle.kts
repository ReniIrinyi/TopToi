import java.io.BufferedReader
import java.io.InputStreamReader

fun startProcess(name: String, directory: File, command: List<String>) {
    Thread {
        try {
            println("▶ Starting a $name: ${command.joinToString(" ")}")

            val process = ProcessBuilder(command)
                .directory(directory)
                .redirectErrorStream(true)
                .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                println("[$name] $line")
            }

            process.waitFor()
        } catch (e: Exception) {
            println("Fehler $name: ${e.message}")
        }
    }.start()
}

tasks.register("runBoth") {
    doLast {
        startProcess(
            name = "Backend",
            directory = file("."),
            command = listOf("./gradlew", ":toi-api:runBackend")
        )

        startProcess(
            name = "Frontend",
            directory = file("toi-client"),
            command = listOf("npm", "run", "dev")
        )

        println("✅ Starting Frontend + Backend erfolgreich.")
        println("⏳ Up and Running... (To stop: Ctrl+C)")

        while (true) {
            Thread.sleep(10_000)
        }
    }
}
