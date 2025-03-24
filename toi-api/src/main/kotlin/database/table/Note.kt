package database.table

import database.table.Vote.references
import org.jetbrains.exposed.sql.Table

object Note : Table("notes") {
    val id = integer("id").autoIncrement()
    val toiletId = integer("toilet_id").references(Toilet.id)
    val userId = integer("user_id").references(User.id)
    val text = varchar("note", 1024)
    val addDate = varchar("add_date", 255)

    override val primaryKey = PrimaryKey(id)
}