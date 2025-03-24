package database

// DatabaseConfig.kt
import com.typesafe.config.ConfigFactory
import database.table.Note
import database.table.Toilet
import database.table.User
import database.table.Vote
import org.jetbrains.exposed.sql.Database
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
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

        transaction {
            SchemaUtils.create(Toilet, Note, Vote, User)
        }
    }
}
