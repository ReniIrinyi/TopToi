package database

// DatabaseConfig.kt
import org.jetbrains.exposed.sql.Database
import io.ktor.server.application.*

object DatabaseConfig {
    fun init(app: Application) {
        val config = app.environment.config
        val url = config.property("database.url").getString()
        val user = config.property("database.user").getString()
        val password = config.property("database.password").getString()
        val driver = config.property("database.driver").getString()

        Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password
        )
    }
}
