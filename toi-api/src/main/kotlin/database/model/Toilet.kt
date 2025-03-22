package database.model

import kotlinx.serialization.Serializable

@Serializable
data class Toilet(
    val id: Int,
    val userId: String = "BUDIPEST-DEFAULT",
    val name: String,
    val addDate: String,
    val category: String? = null,
    val openHours: List<Int> = emptyList(),
    val tags: Tags = Tags(),
    val entryMethod: String? = null,
    val priceHUF: Double? = null,
    val priceEUR: Double? = null,
    val code: String? = null,
    val latitude: Double,
    val longitude: Double,
    val notes: List<Note> = emptyList(),
    val votes: List<Vote> = emptyList()
)

@Serializable
data class Tags(
    val BABY_ROOM: Boolean = false,
    val WHEELCHAIR_ACCESSIBLE: Boolean = false
)