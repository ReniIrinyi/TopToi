package database.table

import org.jetbrains.exposed.sql.Table

object Vote: Table("votes") {
    val id = integer("id").autoIncrement()
    val toiletId = integer("toilet_id").references(Toilet.id)
    val userId = integer("user_id").references(User.id)
    val value = integer("value")

    override val primaryKey = PrimaryKey(id)
}