package database.table

import org.jetbrains.exposed.sql.Table

object User: Table("user") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255)
    val password = varchar("password", 255).nullable()
    val authProvider = varchar("authProvider", 255)
    val name = varchar("name", 255).nullable()
    val imgUrl = varchar("imgUrl", 255).nullable()
    val img = blob("img").nullable()

    override val primaryKey = PrimaryKey(id)
}