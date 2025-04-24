package dto

import kotlinx.serialization.Serializable

@Serializable
data class VoteDTO(
    val id: Int,
    val toiletId: Int,
    val userId: Int,
    val value: Boolean
)