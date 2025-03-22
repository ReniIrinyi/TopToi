package database

// DatabaseConfig.kt
import com.typesafe.config.ConfigFactory
import org.jetbrains.exposed.sql.Database
import io.ktor.server.application.*
import java.io.File


object DatabaseConfig {
    fun init(app: Application) {
        val fileConfig = ConfigFactory.parseFile(File("src/main/resources/application.conf"))
        val dbUrl = fileConfig.getString("database.url")
        val user =fileConfig.getString("database.user")
        val password =  fileConfig.getString("database.password")
        val driver =  fileConfig.getString("database.driver")

        Database.connect(
            url = dbUrl,
            driver = driver,
            user = user,
            password = password
        )
    }
}
