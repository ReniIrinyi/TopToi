package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TagModel(
    val toiletId: Int? = null,
    val babyRoom: Boolean? = null,
    val accessible: Boolean? = null
)