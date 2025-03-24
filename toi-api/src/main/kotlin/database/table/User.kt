package database.table

import database.table.Toilet.autoIncrement
import org.jetbrains.exposed.sql.Table

object User: Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255)
    val passwordHash = varchar("password", 255)

    override val primaryKey = PrimaryKey(id)
}