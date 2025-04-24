package database.model

import kotlinx.serialization.Serializable

@Serializable
data class TagModel(
    val toiletId: Int,
    val babyRoom: Boolean,
    val accessible: Boolean
)