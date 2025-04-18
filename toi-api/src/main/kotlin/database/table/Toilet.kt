package database.table

import org.jetbrains.exposed.sql.Table

object Toilet : Table("toilets") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val addDate = varchar("add_date", 50)
    val category = varchar("category", 255).nullable()
    val entryMethod = varchar("entry_method", 255).nullable()
    val priceCHF = double("price_chf").nullable()
    val code = varchar("code", 255).nullable()
    val latitude = double("latitude")
    val longitude = double("longitude")
    val babyRoom = bool("baby_room").default(false)
    val wheelchairAccessible = bool("wheelchair_accessible").default(false)

    override val primaryKey = PrimaryKey(id)
}