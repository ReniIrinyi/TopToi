package database.table

import org.jetbrains.exposed.sql.Table

object User: Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255)
    val passwordHash = varchar("password", 255)
    val authProvider = enumerationByName("authProvider", 20, AuthProvider::class)
    val name = varchar("name", 255)
    val imgUrl = varchar("imgUrl", 255)
    val img = blob("img")

    override val primaryKey = PrimaryKey(id)
}