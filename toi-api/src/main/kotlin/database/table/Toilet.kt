package database.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Toilet : Table("toilet") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val addDate = timestamp("add_date")
    val category = varchar("category", 255).nullable()
    val entryMethod = varchar("entry_method", 255).nullable()
    val priceCHF = double("price_chf").nullable()
    val code = varchar("code", 255).nullable()
    val latitude = double("latitude")
    val longitude = double("longitude")

    override val primaryKey = PrimaryKey(id)
}