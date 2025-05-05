package adapter.persistence.database.table
import org.jetbrains.exposed.sql.Table

object Tag : Table("tag") {
    val id = integer("id").autoIncrement()
    val toiletId = integer("toilet_id").references(Toilet.id)
    val babyRoom = bool("baby_room").nullable()
    val accessible = bool("accessible").nullable()

    override val primaryKey = PrimaryKey(id)
}