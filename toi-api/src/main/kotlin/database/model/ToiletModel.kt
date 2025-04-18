package database.model

import kotlinx.serialization.Serializable

@Serializable
data class ToiletModel(
    val id: Int,
    val userId: String = "-1",
    val name: String,
    val addDate: String,
    val category: String? = null,
    val openHours: List<Int> = emptyList(),
    val tags: Tags = Tags(),
    val entryMethod: String? = null,
    val priceCHF: Double? = null,
    val code: String? = null,
    val latitude: Double,
    val longitude: Double,
    val notes: List<NoteModel> = emptyList(),
    val votes: List<VoteModel> = emptyList()
)

@Serializable
data class Tags(
    val BABY_ROOM: Boolean = false,
    val WHEELCHAIR_ACCESSIBLE: Boolean = false
)