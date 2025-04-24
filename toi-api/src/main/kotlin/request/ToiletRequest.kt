package request

import dto.TagDTO
import kotlinx.serialization.Serializable

@Serializable
data class ToiletRequest(
    val name: String,
    val category: String?,
    val entryMethod: String?,
    val priceCHF: Double?,
    val code: String?,
    val latitude: Double,
    val longitude: Double,
    val tags: TagDTO? = null,
    val vote: Int? = null,
    val note: String? = null,
    val img: String? = null
)
