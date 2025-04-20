package database.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Note : Table("notes") {
    val id = integer("id").autoIncrement()
    val addDate = timestamp("add_date")
    val toiletId = integer("toilet_id").references(Toilet.id)
    val userId = integer("user_id").references(User.id)
    val note = varchar("note", 1024)
    val img = blob("img")

    override val primaryKey = PrimaryKey(id)
}