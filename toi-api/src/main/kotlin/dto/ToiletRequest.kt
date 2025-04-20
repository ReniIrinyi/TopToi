package dto

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
    val vote: Int? = null,
    val note: String? = null,
    val imageBase64: String? = null
)

@Serializable
data class TagsRequest(
    val BABY_ROOM: Boolean = false,
    val WHEELCHAIR_ACCESSIBLE: Boolean = false
)