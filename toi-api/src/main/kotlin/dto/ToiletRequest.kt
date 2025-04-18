package dto

import database.model.NoteModel
import database.model.VoteModel
import kotlinx.serialization.Serializable

@Serializable
data class ToiletRequest(
    val name: String,
    val addDate: String,
    val category: String? = null,
    val openHours: List<Int> = emptyList(),
    val tags: TagsRequest = TagsRequest(),
    val entryMethod: String? = null,
    val priceCHF: Double? = null,
    val code: String? = null,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class TagsRequest(
    val BABY_ROOM: Boolean = false,
    val WHEELCHAIR_ACCESSIBLE: Boolean = false
)